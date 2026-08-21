package dev.kaooot.debugger.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import dev.kaooot.debugger.api.config.Config;
import dev.kaooot.debugger.core.registry.Registry;
import dev.kaooot.debugger.core.registry.RegistryKey;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ConfigRegistry extends Registry<String, Config> {

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final Map<Class<? extends Config>, String> cache = new Object2ObjectOpenHashMap<>();

    @Override
    public RegistryKey getKey() {
        return RegistryKey.CONFIG;
    }

    public <T extends Config> void save(T config) {
        try (final FileOutputStream outputStream = new FileOutputStream("data/" +
            config.getName() + ".json")) {
            outputStream.write(this.gson.toJson(config).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T extends Config> T reload(Class<T> clazz) {
        return this.load(clazz);
    }

    public <T extends Config> T get(Class<T> clazz) {
        return (T) this.getValue(this.cache.get(clazz));
    }

    @Override
    protected void register(Config config) {
        this.registry.put(config.getName(), this.load(config.getClass(), config.getDefaults()));
        this.cache.put(config.getClass(), config.getName());
    }

    @Override
    protected boolean validateType(Class<?> clazz) {
        return clazz.getSuperclass().isAssignableFrom(Config.class);
    }

    private <T extends Config> T load(Class<T> clazz) {
        try {
            return this.load(clazz, clazz.getConstructors()[0].newInstance());
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends Config> T load(Class<T> clazz, Object defaultConfig) {
        final File file = new File("data/" + ((Config) defaultConfig).getName() + ".json");
        try (final InputStream inputStream = new FileInputStream(file)) {
            try (final InputStreamReader reader = new InputStreamReader(inputStream)) {
                return this.gson.fromJson(reader, clazz);
            }
        } catch (Throwable e) {
            if (!file.exists()) {
                try (final FileOutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(this.gson.toJson(defaultConfig)
                        .getBytes(StandardCharsets.UTF_8));
                    return (T) defaultConfig;
                } catch (IOException ex) {
                    e.printStackTrace();
                }
            }
        }
        throw new RuntimeException("Failed to load configuration " + clazz.getName());
    }
}