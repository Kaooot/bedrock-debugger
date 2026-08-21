package dev.kaooot.debugger.core.registry;

import com.google.common.reflect.ClassPath;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import dev.kaooot.debugger.network.PacketHandlerRegistry;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class Registries {

    private static final Map<RegistryKey, Registry<?, ?>> REGISTRIES =
        new Object2ObjectOpenHashMap<>();

    public static void init() {
        final String basePackageName = "dev.kaooot.debugger";
        try {
            for (final ClassPath.ClassInfo classInfo : ClassPath.from(
                    PacketHandlerRegistry.class.getClassLoader())
                .getTopLevelClassesRecursive(basePackageName)) {
                final Class<?> clazz = classInfo.load();

                if (clazz.getSuperclass() == null || clazz.getSuperclass().equals(Object.class) ||
                    !clazz.getSuperclass().isAssignableFrom(Registry.class)) {
                    continue;
                }

                final Registry<?, ?> registry = (Registry<?, ?>) clazz.getConstructors()[0]
                    .newInstance();
                registry.init(basePackageName + "." + registry.getKey().getSubPackageName());

                REGISTRIES.put(registry.getKey(), registry);
            }
        } catch (IOException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static <T extends Registry<?, ?>> T getRegistry(RegistryKey key) {
        return (T) REGISTRIES.get(key);
    }
}