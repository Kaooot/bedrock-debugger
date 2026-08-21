package dev.kaooot.debugger.command.internal;

import com.google.gson.JsonArray;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.annotation.Description;
import dev.kaooot.debugger.api.command.annotation.Name;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.TestConfig;
import dev.kaooot.debugger.core.memory.CoreMemory;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Name("dumpsounds")
@Description("Outputs valid level sound events in a json format.")
public class DumpSoundsCommand extends GenerateCommand {

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        final String path = Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(TestConfig.class).getDebugServerPath();

        if (path == null || path.isEmpty()) {
            proxy.getPlayer().sendMessage("The provided debug server path is invalid.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (final CoreMemory coreMemory = new CoreMemory(Path.of(
                new File(proxy.getDataFolder(), path).getAbsolutePath() + "/bedrock_server.exe"
            ))) {
                final JsonArray levelSoundEvents = new JsonArray();
                for (final String levelSoundEvent : coreMemory.getLevelSoundEvents()) {
                    levelSoundEvents.add(levelSoundEvent);
                }

                final File outputFile = new File(
                    proxy.getDataLogsFolder(), "level_sound_events.json"
                );

                this.saveJsonFile(
                    outputFile,
                    proxy.getGson().toJson(levelSoundEvents).getBytes(StandardCharsets.UTF_8),
                    proxy,
                    JsonArray.class
                );

                proxy.getPlayer().sendMessage(
                    "Success! :) File output to: " + outputFile.getAbsolutePath()
                );
            } catch (IOException e) {
                e.printStackTrace();
                proxy.getPlayer().sendMessage("§cFailure :( Error: " + e.getMessage());
            }
        });
    }
}