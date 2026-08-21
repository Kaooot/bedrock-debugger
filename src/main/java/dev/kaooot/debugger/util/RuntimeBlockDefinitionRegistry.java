package dev.kaooot.debugger.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Value;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.common.DefinitionRegistry;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class RuntimeBlockDefinitionRegistry implements DefinitionRegistry<BlockDefinition> {

    private final Int2ObjectMap<BlockDefinition> definitions = new Int2ObjectOpenHashMap<>();

    public RuntimeBlockDefinitionRegistry() {

    }

    @Override
    public BlockDefinition getDefinition(int runtimeId) {
        return this.definitions.computeIfAbsent(runtimeId, RuntimeBlockDefinition::new);
    }

    @Override
    public int getRuntimeIdByName(String name) {
        return -1;
    }

    @Override
    public boolean isRegistered(BlockDefinition definition) {
        return this.definitions.containsValue(definition);
    }

    @Value
    public static class RuntimeBlockDefinition implements BlockDefinition {
        int runtimeId;
    }
}