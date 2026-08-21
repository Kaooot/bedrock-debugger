package dev.kaooot.debugger.core.registry;

import com.google.common.reflect.ClassPath;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import dev.kaooot.debugger.api.logging.Logger;
import dev.kaooot.debugger.logging.MainLogger;
import dev.kaooot.debugger.network.PacketHandlerRegistry;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public abstract class Registry<K, V> {

    protected final Map<K, V> registry = new Object2ObjectOpenHashMap<>();

    private boolean initialized;

//    private final Logger logger = new MainLogger();

    public void init(String packageName) {
        if (this.initialized) {
            throw new RuntimeException("A registry cannot be re-initialized during runtime");
        }
        final long timestamp = System.currentTimeMillis();
        try {
            for (final ClassPath.ClassInfo classInfo : ClassPath.from(
                    PacketHandlerRegistry.class.getClassLoader())
                .getTopLevelClassesRecursive(packageName)) {
                final Class<?> clazz = classInfo.load();
                final int modifiers = clazz.getModifiers();

                if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers) ||
                    !this.validateType(clazz)) {
                    continue;
                }

                for (final Constructor<?> constructor : clazz.getConstructors()) {
                    if (constructor.getParameterCount() == 0) {
                        this.register((V) constructor.newInstance());
                        break;
                    }
                }
            }
            this.initialized = true;
            /*this.logger.debug(
                "Completed registration in {} ms, RegistryName: {}",
                System.currentTimeMillis() - timestamp,
                this.getClass().getSimpleName()
            );*/
        } catch (IOException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public V getValue(K key) {
        return this.registry.get(key);
    }

    public Set<K> getKeys() {
        return Collections.unmodifiableSet(this.registry.keySet());
    }

    public Set<V> getValues() {
        return Set.copyOf(this.registry.values());
    }

    public abstract RegistryKey getKey();

    protected abstract void register(V type);

    protected abstract boolean validateType(Class<?> clazz);
}