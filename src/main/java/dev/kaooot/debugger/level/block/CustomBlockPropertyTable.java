package dev.kaooot.debugger.level.block;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.util.BlockUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class CustomBlockPropertyTable {

    private static final Map<String, List<BlockTrait<?>>> BLOCK_PROPERTIES =
        new Object2ObjectOpenHashMap<>();

    private static final Set<String> DIRECTIONS = Set.of(
        "down",
        "up",
        "north",
        "south",
        "west",
        "east"
    );
    private static final Set<String> CARDINAL_DIRECTIONS = Set.of(
        "south",
        "west",
        "north",
        "east"
    );

    static {
        BLOCK_PROPERTIES.put(
            "block_face",
            Collections.singletonList(
                new BlockTrait<>(
                    "minecraft:block_face", DIRECTIONS
                )
            )
        );
        BLOCK_PROPERTIES.put(
            "vertical_half",
            Collections.singletonList(
                new BlockTrait<>(
                    "minecraft:vertical_half", Set.of("bottom", "top")
                )
            )
        );
        BLOCK_PROPERTIES.put(
            "cardinal_direction",
            Collections.singletonList(
                new BlockTrait<>(
                    "minecraft:cardinal_direction",
                    CARDINAL_DIRECTIONS
                )
            )
        );
        BLOCK_PROPERTIES.put(
            "facing_direction",
            Collections.singletonList(
                new BlockTrait<>(
                    "minecraft:facing_direction",
                    DIRECTIONS
                )
            )
        );
        BLOCK_PROPERTIES.put(
            "sixteen_way_rotation",
            Collections.singletonList(
                new BlockTrait<>(
                    "minecraft:sixteen_way_rotation",
                    Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
                )
            )
        );
        BLOCK_PROPERTIES.put(
            "corner_and_cardinal_direction",
            Arrays.asList(
                new BlockTrait<>(
                    "minecraft:cardinal_direction",
                    CARDINAL_DIRECTIONS
                ),
                new BlockTrait<>(
                    "minecraft:corner",
                    Set.of("none", "inner_left", "inner_right", "outer_left", "outer_right")
                )
            )
        );
        BLOCK_PROPERTIES.put(
            "cardinal_connections",
            Arrays.asList(
                new BlockTrait<>(
                    "minecraft:connection_east",
                    Set.of((byte) 0, (byte) 1)
                ),
                new BlockTrait<>(
                    "minecraft:connection_north",
                    Set.of((byte) 0, (byte) 1)
                ),
                new BlockTrait<>(
                    "minecraft:connection_south",
                    Set.of((byte) 0, (byte) 1)
                ),
                new BlockTrait<>(
                    "minecraft:connection_west",
                    Set.of((byte) 0, (byte) 1)
                )
            )
        );
        BLOCK_PROPERTIES.put(
            "multi_block_part",
            Collections.singletonList(
                new BlockTrait<>(
                    "minecraft:multi_block_part",
                    Set.of(0, 1, 2, 3)
                )
            )
        );
    }

    private final BedrockDebuggerProxy proxy;

    public List<NbtMap> resolvePermutations(String name, NbtMap properties) {
        final List<BlockTrait<?>> enabledTraits = new ObjectArrayList<>();
        final NbtMap emptyState = this.buildEmptyState(name);
        if (properties.containsKey("traits")) {
            final List<NbtMap> traits = properties.getList("traits", NbtType.COMPOUND);
            for (final NbtMap trait : traits) {
                if (!trait.containsKey("enabled_states")) {
                    continue;
                }
                final NbtMap enabledStates = trait.getCompound("enabled_states");
                for (final String stateKey : enabledStates.keySet()) {
                    if (!enabledStates.getBoolean(stateKey)) {
                        continue;
                    }
                    if (!BLOCK_PROPERTIES.containsKey(stateKey)) {
                        this.proxy.getLogger().debug(
                            "Found an unregistered custom block trait: {}",
                            stateKey
                        );
                        continue;
                    }
                    enabledTraits.addAll(BLOCK_PROPERTIES.get(stateKey));
                }
            }
        }
        if (enabledTraits.isEmpty()) {
            return Collections.singletonList(emptyState);
        }
        List<NbtMap> traits = new ObjectArrayList<>();
        traits.add(NbtMap.EMPTY);
        for (final BlockTrait<?> trait : enabledTraits) {
            final String traitName = trait.getName();
            final List<NbtMap> next = new ObjectArrayList<>();
            for (final NbtMap partial : traits) {
                for (final Object value : trait.getValues()) {
                    final NbtMapBuilder builder = partial.toBuilder();
                    switch (value) {
                        case String string -> builder.putString(traitName, string);
                        case Integer integer -> builder.putInt(traitName, integer);
                        case Byte b -> builder.putByte(traitName, b);
                        default -> throw new IllegalStateException(
                            "Invalid block trait type detected: " + value.getClass()
                        );
                    }
                    final NbtMap states = builder.build();
                    next.add(
                        NbtMap.builder()
                            .putInt("network_id", BlockUtil.createHash(name, states))
                            .putString("name", name)
                            .putCompound("states", states)
                            .build()
                    );
                }
            }
            traits = next;
        }
        return traits;
    }

    private NbtMap buildEmptyState(String name) {
        return NbtMap.builder()
            .putInt("network_id", BlockUtil.createHash(name, NbtMap.EMPTY))
            .putString("name", name)
            .putCompound("states", NbtMap.EMPTY)
            .build();
    }

    @Value
    private static class BlockTrait<T> {
        String name;
        Set<T> values;
    }
}