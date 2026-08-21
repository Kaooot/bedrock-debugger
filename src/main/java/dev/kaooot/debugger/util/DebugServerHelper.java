package dev.kaooot.debugger.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.TestConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class DebugServerHelper {

    private final BedrockDebuggerProxy proxy;
    private static final AtomicInteger PORT_COUNTER = new AtomicInteger(50000);

    private Process process;

    @Getter
    private long processId = -1;

    public CompletableFuture<Void> startDebugServer() {
        return CompletableFuture.supplyAsync(() -> {
            final String path = Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG)
                .get(TestConfig.class).getDebugServerPath();
            if (path == null || path.isEmpty()) {
                throw new IllegalStateException("The provided debug server path is invalid.");
            }
            final File source = new File(this.proxy.getDataFolder(), path);

            this.updateProperties(source.toPath());

            try {
                this.process = Runtime.getRuntime().exec(source.getAbsolutePath() +
                    "\\bedrock_server.exe");
                this.processId = this.process.pid();

                CompletableFuture.runAsync(() -> {
                    while (this.process.isAlive()) {
                        final InputStream inputStream = this.process.getInputStream();
                        if (inputStream != null) {
                            try (final InputStreamReader inputStreamReader =
                                     new InputStreamReader(inputStream);
                                 final BufferedReader reader = new BufferedReader(
                                     inputStreamReader)) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.out.println(line);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public void stopDebugServer() {
        if (this.process == null) {
            throw new IllegalStateException("Debug server is not running.");
        }
        this.process.destroy();
        this.processId = -1;
    }

    private void updateProperties(Path path) {
        try (final Stream<Path> stream = Files.walk(path)
            .filter(p -> p.toFile().getName().equalsIgnoreCase("server.properties"))) {
            final File serverPropertiesFile = stream.findFirst()
                .orElseThrow(
                    () -> new IllegalStateException("Unable to find server.properties file")
                )
                .toFile();

            final Properties properties = new Properties();

            try (final FileInputStream inputStream = new FileInputStream(serverPropertiesFile)) {
                properties.load(inputStream);
            }

            properties.setProperty("server-port", String.valueOf(PORT_COUNTER.incrementAndGet()));
            properties.setProperty("server-portv6", String.valueOf(PORT_COUNTER.incrementAndGet()));

            try (final FileOutputStream outputStream = new FileOutputStream(serverPropertiesFile)) {
                properties.store(outputStream, "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}