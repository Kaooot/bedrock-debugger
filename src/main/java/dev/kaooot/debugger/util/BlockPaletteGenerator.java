package dev.kaooot.debugger.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NBTOutputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.NbtUtils;
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
public class BlockPaletteGenerator {

    private static final List<String> EXCEPTIONS = Arrays.asList(
        "minecraft:chalkboard",
        "minecraft:deprecated_anvil",
        "minecraft:deprecated_purpur_block_2",
        "minecraft:deprecated_purpur_block_1"
    );

    private final BedrockDebuggerProxy proxy;

    public File generate(byte[] blockDump) {
        final String bdsPath = Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(TestConfig.class).getDebugServerPath();
        final File file = new File(this.proxy.getDataFolder(), bdsPath);
        final File statePrioritiesFile = new File(
            this.proxy.getDataFolder(),
            "state_priorities.json"
        );
        final File structureFile = new File(this.proxy.getDataFolder(), "test.mcstructure");

        try (final Stream<Path> stream = Files.walk(file.toPath())
            .filter(p -> p.toFile().getName().equalsIgnoreCase("mojang-blocks.json"))) {
            final File mojangBlocksFile = stream.findFirst()
                .orElseThrow(
                    () -> new IllegalStateException("Unable to find mojang-blocks.json file")
                )
                .toFile();

            this.proxy.getLogger().debug("Generating block palette for {} ...", bdsPath);

            final List<NbtMap> blocks = new ObjectArrayList<>();
            final Set<String> blockNames = new HashSet<>();
            final Map<String, Set<String>> actualStates = new HashMap<>();
            final Map<String, Map<String, List<Object>>> allProperties = new HashMap<>();
            final Map<String, List<Object>> propertiesAndValues = new HashMap<>();
            final Map<String, Map<String, Integer>> statePriorities = new HashMap<>();

            this.readMojangBlocks(
                mojangBlocksFile, blockNames, actualStates, allProperties, propertiesAndValues
            );
            this.readStatePriorities(statePrioritiesFile, statePriorities);

            final Map<String, List<NbtMapBuilder>> blockPermutations = new HashMap<>();
            final Map<String, Map<String, List<Object>>> map = new HashMap<>();

            this.readBlockDump(
                blockDump, blockPermutations, actualStates, allProperties, map
            );

            final IntList processedNetworkIds = new IntArrayList();
            final int blockPaletteVersion = this.getBlockPaletteVersion(structureFile);

            this.registerStates(
                blockPermutations, actualStates, processedNetworkIds, blocks,
                blockPaletteVersion
            );
            this.registerExceptionStates(
                propertiesAndValues, processedNetworkIds, blocks, blockPaletteVersion
            );
            this.sort(blocks, statePriorities, propertiesAndValues);
            this.writeBlockPaletteJSON(blocks);
            this.writeBlockPaletteNBT(blocks);
            return this.getOutputFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private File getOutputFile() {
        return new File(this.proxy.getDataLogsFolder(), "block_palette.nbt");
    }

    private void readMojangBlocks(File mojangBlocks, Set<String> blockNames,
                                  Map<String, Set<String>> actualStates,
                                  Map<String, Map<String, List<Object>>> allProperties,
                                  Map<String, List<Object>> propertiesAndValues) {
        try (final FileReader reader = new FileReader(mojangBlocks)) {
            final JsonObject jsonObject = this.proxy.getGson().fromJson(reader, JsonObject.class);

            this.proxy.getLogger().debug("Mojang block palette version: " +
                jsonObject.get("minecraft_version").getAsString());

            final JsonArray blockProperties = jsonObject.getAsJsonArray("block_properties");

            for (JsonElement element : blockProperties) {
                final JsonObject blockProperty = element.getAsJsonObject();
                final String name = blockProperty.get("name").getAsString();
                final String type = blockProperty.get("type").getAsString();
                final JsonArray values = blockProperty.getAsJsonArray("values");

                final List<Object> propertyValues = new LinkedList<>();
                for (JsonElement element1 : values) {
                    final JsonElement value = element1.getAsJsonObject().get("value");
                    if (type.equalsIgnoreCase("int")) {
                        propertyValues.add(value.getAsInt());
                    } else if (type.equalsIgnoreCase("bool")) {
                        propertyValues.add(value.getAsBoolean());
                    } else if (type.equalsIgnoreCase("string")) {
                        propertyValues.add(value.getAsString());
                    }
                }
                propertiesAndValues.computeIfAbsent(name, s -> new ArrayList<>())
                    .addAll(propertyValues);
            }

            final JsonArray dataItems = jsonObject.getAsJsonArray("data_items");

            for (JsonElement dataItem : dataItems) {
                final JsonObject block = dataItem.getAsJsonObject();
                final String name = block.get("name").getAsString();

                for (JsonElement property : block.getAsJsonArray("properties")) {
                    final String propertyName =
                        property.getAsJsonObject().get("name").getAsString();
                    actualStates.computeIfAbsent(name, s -> new HashSet<>()).add(propertyName);
                    allProperties.computeIfAbsent(name, s -> new HashMap<>()).put(propertyName,
                        propertiesAndValues.get(propertyName));
                }
                blockNames.add(name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readBlockDump(byte[] blockDump,
                               Map<String, List<NbtMapBuilder>> blockPermutations,
                               Map<String, Set<String>> actualStates,
                               Map<String, Map<String, List<Object>>> allProperties,
                               Map<String, Map<String, List<Object>>> map) {
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(blockDump)) {
            final JsonObject jsonObject = this.proxy.getGson()
                .fromJson(new String(inputStream.readAllBytes()), JsonObject.class);
            final JsonArray blocks = jsonObject.getAsJsonArray("blocks");
            final JsonArray oldFormatBlocks = new JsonArray();

            for (final JsonElement block : blocks) {
                final JsonObject obj = block.getAsJsonObject();
                final String name = obj.get("name").getAsString();
                final JsonArray states = obj.getAsJsonArray("states");
                final JsonObject statesObj = new JsonObject();
                final JsonObject blockState = new JsonObject();
                blockState.addProperty("name", name);

                for (final JsonElement e : states) {
                    final JsonObject state = e.getAsJsonObject();
                    final String stateName = state.get("name").getAsString();
                    final JsonArray values = state.getAsJsonArray("values");

                    final JsonArray jsonArray = new JsonArray();
                    jsonArray.addAll(values);

                    statesObj.add(stateName, jsonArray);
                }

                blockState.add("states", statesObj);

                oldFormatBlocks.add(blockState);
            }

            for (JsonElement element : oldFormatBlocks) {
                if (!element.isJsonObject()) {
                    continue;
                }

                final JsonObject block = element.getAsJsonObject();
                final String name = block.get("name").getAsString();

                    /*if (!blockNames.contains(name)) {
                        System.out.println(name + " is not present in mojang blocks");
                        continue;
                    }*/

                JsonObject obj = block.getAsJsonObject("states");

                final Set<String> stateNames = actualStates.getOrDefault(name, new HashSet<>());
                for (String stateName : stateNames) {
                    if (obj == null || !obj.has(stateName)) {
                            /*System.out.println("missing state found for " + name + ": " +
                                stateName);*/
                        final JsonArray jsonArray1 = new JsonArray();
                        for (Object o : allProperties.get(name).get(stateName)) {
                            if (o instanceof Number number) {
                                jsonArray1.add(number);
                            } else if (o instanceof Boolean b) {
                                jsonArray1.add(b);
                            } else {
                                jsonArray1.add(o.toString());
                            }
                        }
                        obj = new JsonObject();
                        obj.add(stateName, jsonArray1);
                    }
                }

                if (obj == null && EXCEPTIONS.stream()
                    .noneMatch(s -> s.equalsIgnoreCase(name))) {
                    blockPermutations.put(name, Collections.singletonList(NbtMap.builder()));
                    continue;
                }

                final List<String> states = new ArrayList<>(stateNames);

                int requiredPermutations = 1;
                for (String stateName : states) {
                    requiredPermutations *= obj.getAsJsonArray(stateName).size();

                    final List<Object> properties = new ObjectArrayList<>();
                    for (JsonElement jsonElement : obj.getAsJsonArray(stateName)) {
                        final String s = jsonElement.getAsString();
                        Object property;
                        try {
                            property = Integer.parseInt(s);
                        } catch (NumberFormatException e) {
                            if (s.equalsIgnoreCase("true") ||
                                s.equalsIgnoreCase("false")) {
                                property = Boolean.parseBoolean(s);
                            } else {
                                property = s;
                            }
                        }
                        properties.add(property);
                    }
                    map.computeIfAbsent(name, s -> new HashMap<>()).put(stateName, properties);
                }

                int permutations = 0;

                final Random random = new Random();

                while (permutations < requiredPermutations) {
                    final Map<String, String> randomProperties = new HashMap<>();
                    for (String stateName : states) {
                        randomProperties.put(stateName, obj.getAsJsonArray(stateName)
                            .get(random.nextInt(obj.getAsJsonArray(stateName).size()))
                            .getAsString());
                    }
                    final NbtMapBuilder statesBuilder = NbtMap.builder();

                    for (Map.Entry<String, String> entry : randomProperties.entrySet()) {
                        readProperty(statesBuilder, entry.getKey(), entry.getValue());
                    }

                    if (!blockPermutations.getOrDefault(name, new ObjectArrayList<>())
                        .contains(statesBuilder)) {
                        blockPermutations.computeIfAbsent(name, s -> new ObjectArrayList<>())
                            .add(statesBuilder);
                        permutations++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readStatePriorities(File file, Map<String, Map<String, Integer>> statePriorities) {
        try (final FileInputStream inputStream = new FileInputStream(file)) {
            final JsonObject jsonObject = this.proxy.getGson()
                .fromJson(new String(inputStream.readAllBytes()), JsonObject.class);
            for (final String name : jsonObject.keySet()) {
                final JsonObject block = jsonObject.getAsJsonObject(name);
                final Map<String, Integer> map = new HashMap<>();
                for (final String stateName : block.keySet()) {
                    map.put(stateName, block.get(stateName).getAsInt());
                }
                statePriorities.put(name, map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerStates(Map<String, List<NbtMapBuilder>> blockPermutations,
                                Map<String, Set<String>> actualStates,
                                IntList processedNetworkIds,
                                List<NbtMap> blocks, int blockPaletteVersion) {
        for (final String exception : EXCEPTIONS) {
            blockPermutations.remove(exception);
        }

        for (String name : blockPermutations.keySet()) {
            for (NbtMapBuilder statesBuilder : blockPermutations.get(name)) {
                final Set<String> statesToRemove = new HashSet<>();
                for (String stateName : statesBuilder.keySet()) {
                    if (!actualStates.getOrDefault(name, new HashSet<>()).contains(stateName)) {
                        statesToRemove.add(stateName);
                    }
                }
                for (String stateName : statesToRemove) {
                    statesBuilder.remove(stateName);
                }

                if (EXCEPTIONS.stream().anyMatch(s -> s.equalsIgnoreCase(name))) {
                    continue;
                }

                registerState(name, statesBuilder, processedNetworkIds, blocks,
                    blockPaletteVersion);
            }
        }
    }

    private void registerExceptionStates(Map<String, List<Object>> propertiesAndValues,
                                         IntList processedNetworkIds,
                                         List<NbtMap> blocks, int blockPaletteVersion) {
        // exceptions since v1.21.50.25
        for (int j = 0; j < 16; j++) {
            final NbtMapBuilder statesBuilder = NbtMap.builder();
            statesBuilder.putInt("direction", j);

            registerState("minecraft:chalkboard", statesBuilder, processedNetworkIds,
                blocks, blockPaletteVersion);
        }

        for (final Object direction : propertiesAndValues.get("minecraft:cardinal_direction")) {
            registerState("minecraft:deprecated_anvil", NbtMap.builder()
                    .putString("minecraft:cardinal_direction", direction.toString()),
                processedNetworkIds,
                blocks,
                blockPaletteVersion
            );
        }

        registerWithPillarAxis("minecraft:deprecated_purpur_block_2", propertiesAndValues,
            processedNetworkIds, blocks, blockPaletteVersion);
        registerWithPillarAxis("minecraft:deprecated_purpur_block_1", propertiesAndValues,
            processedNetworkIds, blocks, blockPaletteVersion);
    }

    private void sort(List<NbtMap> blocks,
                      Map<String, Map<String, Integer>> statePriorities,
                      Map<String, List<Object>> propertiesAndValues) {
        blocks.sort((o1, o2) -> Long.compareUnsigned(o1.getLong("name_hash"),
            o2.getLong("name_hash")));
        blocks.sort((o1, o2) -> {
            final int compare = Long.compareUnsigned(o1.getLong("name_hash"),
                o2.getLong("name_hash"));
            if (compare == 0) {
                final String name = o1.getString("name");
                final NbtMap states1 = o1.getCompound("states");
                final NbtMap states2 = o2.getCompound("states");
                final List<String> stateNames = new ObjectArrayList<>(states1.keySet());
                final Map<String, Integer> priorities = statePriorities.get(name);
                if (priorities == null && states1.size() > 1) {
                    throw new RuntimeException("Priorities for " + name + " " + stateNames +
                        " not found");
                }
                if (priorities != null) {
                    for (final String stateName : stateNames) {
                        if (!priorities.containsKey(stateName)) {
                            throw new RuntimeException("State name " + stateName +
                                " not found in priorities for " + name);
                        }
                    }
                    stateNames.sort((name1, name2) -> Integer.compare(priorities.get(name2),
                        priorities.get(name1)));
                }
                for (final String stateName : stateNames) {
                    final Object stateValue1 = states1.get(stateName);
                    final Object stateValue2 = states2.get(stateName);
                    final boolean last = stateNames.indexOf(stateName) == stateNames.size() - 1;

                    int cmp = 0;
                    if (stateValue1 instanceof Byte b) {
                        cmp = Byte.compare(b, (byte) stateValue2);
                    } else if (stateValue1 instanceof Integer integer) {
                        cmp = Integer.compare(integer, (int) stateValue2);
                    } else if (stateValue1 instanceof String) {
                        final int j = propertiesAndValues.get(stateName)
                            .indexOf(stateValue1.toString());
                        final int k = propertiesAndValues.get(stateName)
                            .indexOf(stateValue2.toString());
                        cmp = Integer.compare(j, k);
                    }
                    if (cmp != 0 || last) {
                        return cmp;
                    }
                }
            }
            return compare;
        });

        // order states by block property names
        final List<NbtMap> temp = new ObjectArrayList<>();
        for (NbtMap block : blocks) {
            temp.add(block.toBuilder()
                .putCompound("states",
                    NbtMap.fromMap(new TreeMap<>(block.getCompound("states"))))
                .build());
        }
        blocks.clear();
        blocks.addAll(temp);
    }

    private void writeBlockPaletteJSON(List<NbtMap> blocks) {

        final JsonObject blockPalette = new JsonObject();
        final JsonArray blocksJson = new JsonArray();
        for (NbtMap block : blocks) {
            final JsonObject blockJson = this.proxy.getBlockPaletteManager().createJSONBlock(block);
            blocksJson.add(blockJson);
        }
        blockPalette.add("blocks", blocksJson);

        final File file = new File(this.proxy.getDataLogsFolder(), "block_palette.json");

        try (final FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(this.proxy.getGson().toJson(blockPalette)
                .getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeBlockPaletteNBT(List<NbtMap> blocks) {
        try (final FileOutputStream fileOutputStream = new FileOutputStream(this.getOutputFile());
             final NBTOutputStream outputStream = NbtUtils.createGZIPWriter(fileOutputStream)) {
            outputStream.writeTag(NbtMap.builder()
                .putList("blocks", NbtType.COMPOUND, blocks)
                .build());
            this.proxy.getLogger().debug("Generated Block Palette");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final long FNV1_64_INIT = 0xcbf29ce484222325L;
    private static final long FNV1_PRIME_64 = 0x100000001b3L;

    private long fnv164(final byte[] data) {
        long hash = FNV1_64_INIT;
        for (final byte datum : data) {
            hash *= FNV1_PRIME_64;
            hash ^= (datum & 0xff);
        }
        return hash;
    }

    private void readProperty(NbtMapBuilder statesBuilder, String stateName,
                              String property) {
        try {
            statesBuilder.putInt(stateName, Integer.parseInt(property));
        } catch (NumberFormatException e) {
            if (property.equalsIgnoreCase("true") || property.equalsIgnoreCase("false")) {
                statesBuilder.putByte(stateName, Boolean.parseBoolean(property) ? (byte) 1 : 0);
            } else {
                statesBuilder.putString(stateName, property);
            }
        }
    }

    private void registerState(String name, NbtMapBuilder statesBuilder,
                               IntList processedNetworkIds, List<NbtMap> blocks,
                               int blockPaletteVersion) {
        final int networkId = BlockUtil.createHash(name, statesBuilder.build());

        if (processedNetworkIds.contains(networkId)) {
            return;
        }

        processedNetworkIds.add(networkId);

        blocks.add(NbtMap.builder()
            .putInt("network_id", networkId)
            .putLong("name_hash", fnv164(name.getBytes(StandardCharsets.UTF_8)))
            .putString("name", name)
            .putInt("version", blockPaletteVersion)
            .putCompound("states", statesBuilder.build())
            .build());
    }

    private void registerWithPillarAxis(String name,
                                        Map<String, List<Object>> propertiesAndValues,
                                        IntList processedNetworkIds, List<NbtMap> blocks,
                                        int blockPaletteVersion) {
        for (final Object object : propertiesAndValues.get("pillar_axis")) {
            this.registerState(name,
                NbtMap.builder()
                    .putString("pillar_axis", object.toString()),
                processedNetworkIds,
                blocks,
                blockPaletteVersion
            );
        }
    }

    private int getBlockPaletteVersion(File file) {
        try (final FileInputStream fileInputStream = new FileInputStream(file);
             final NBTInputStream inputStream = NbtUtils.createReaderLE(fileInputStream)) {
            final NbtMap nbtMap = (NbtMap) inputStream.readTag();
            final int blockPaletteVersion = nbtMap.getCompound("structure")
                .getCompound("palette")
                .getCompound("default")
                .getList("block_palette", NbtType.COMPOUND)
                .get(0)
                .getInt("version");
            final int major = (blockPaletteVersion >> 24) & 0xff;
            final int minor = (blockPaletteVersion >> 16) & 0xff;
            final int patch = (blockPaletteVersion >> 8) & 0xff;
            final int revision = blockPaletteVersion & 0xff;
            this.proxy.getLogger().debug("Detected Block Palette Version {} ({}.{}.{}.{})",
                blockPaletteVersion, major, minor, patch, revision);
            return blockPaletteVersion;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(
            "Unable to get block palette version from file: " + file.getAbsolutePath()
        );
    }
}