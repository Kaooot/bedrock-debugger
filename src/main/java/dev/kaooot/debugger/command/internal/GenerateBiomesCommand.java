package dev.kaooot.debugger.command.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
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
@Name("generatebiomes")
@Description("Outputs valid biome ids in a json format.")
public class GenerateBiomesCommand extends GenerateCommand {

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
            .filter(p -> p.toFile().getName().equalsIgnoreCase("mojang-biomes.json"))) {
            final File mojangBiomesFile = stream.findFirst()
                .orElseThrow(
                    () -> new IllegalStateException("Unable to find mojang-biomes.json file")
                )
                .toFile();

            try (final FileInputStream inputStream = new FileInputStream(mojangBiomesFile)) {
                final JsonObject jsonObject = proxy.getGson()
                    .fromJson(new String(inputStream.readAllBytes()), JsonObject.class);
                final String version = jsonObject.get("minecraft_version").getAsString();
                final JsonArray dataItems = jsonObject.getAsJsonArray("data_items");
                final Map<String, Integer> map = new HashMap<>();
                for (final JsonElement element : dataItems) {
                    final JsonObject dataItem = element.getAsJsonObject();
                    final String name = dataItem.get("name").getAsString();
                    final int id = dataItem.get("id").getAsInt();

                    map.put(name, id);
                }

                final Map<String, Integer> sortedMap = map.entrySet()
                    .stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getValue))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue, (k, v) -> k, LinkedHashMap::new
                        )
                    );

                final File outputFile = new File(proxy.getDataLogsFolder(), "biomes.json");

                proxy.getLogger().debug("Detected biomes version: " + version);

                try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    outputStream.write(proxy.getGson().toJson(sortedMap)
                        .getBytes(StandardCharsets.UTF_8));

                    proxy.getPlayer().sendMessage(
                        "Success! :) File output to: " + outputFile.getAbsolutePath()
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            proxy.getPlayer().sendMessage("§cFailure :( Error: " + e.getMessage());
        }
    }
}