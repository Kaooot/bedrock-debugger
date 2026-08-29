package dev.kaooot.debugger.command.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.annotation.Description;
import dev.kaooot.debugger.api.command.annotation.Name;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.TestConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Name("parseprotocoldocs")
@Description("Outputs valid svg files for packet schema descriptions")
public class ParseProtocolDocsCommand extends GenerateCommand {

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        final String path = Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(TestConfig.class).getDebugServerPath();

        if (path == null || path.isEmpty()) {
            proxy.getPlayer().sendMessage("The provided debug server path is invalid.");
            return;
        }

        final File file = new File(proxy.getDataFolder(), path);
        try (final Stream<Path> stream = Files.walk(file.toPath())
            .filter(p -> p.toFile().getName().equalsIgnoreCase("protocol"))) {
            final File protocolFolder = stream.findFirst()
                .orElseThrow(
                    () -> new IllegalStateException("Failed to find protocol folder")
                )
                .toFile();

            CompletableFuture.supplyAsync(() -> proxy.getProtocolDocsParser().parse(
                    proxy.getDataLogsFolder(), protocolFolder.toPath()
                )
            ).whenComplete((outputFolder, throwable) -> {
                if (throwable != null) {
                    proxy.getPlayer().sendMessage("§cFailure :( Error: " + throwable.getMessage());
                    return;
                }
                proxy.getPlayer().sendMessage(
                    "Success! :) File output to: " + outputFolder.getAbsolutePath()
                );
            });
        } catch (IOException e) {
            e.printStackTrace();
            proxy.getPlayer().sendMessage("§cFailure :( Error: " + e.getMessage());
        }
    }
}