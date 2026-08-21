package dev.kaooot.debugger.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.NbtUtils;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.level.block.Block;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class BlockPaletteManager {

    private final BedrockDebuggerProxy proxy;

    @Getter
    private NbtMap blockPalette;

    @Getter
    private JsonObject blockPaletteJson;

    private final Object2ObjectMap<NbtMap, JsonObject> mapping = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Block> blocks = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Block> usedBlocksList = new Int2ObjectOpenHashMap<>();

    public void setUsedBlocksList() {
        final boolean hashes = this.proxy.getPlayer().isBlockNetworkIdsAreHashes();
        for (final Int2ObjectMap.Entry<Block> entry : this.blocks.int2ObjectEntrySet()) {
            if (hashes == entry.getValue().isHashed()) {
                this.usedBlocksList.put(entry.getIntKey(), entry.getValue());
            }
        }
        for (final Block value : this.usedBlocksList.values()) {
            if (value.getState().getString("name").equalsIgnoreCase("minecraft:air")) {
                Block.AIR = value;
                break;
            }
        }
    }

    public Block getBlock(int blockRuntimeId) {
        return this.usedBlocksList.getOrDefault(
            blockRuntimeId,
            new Block(blockRuntimeId, null, this.proxy.getPlayer().isBlockNetworkIdsAreHashes())
        );
    }

    public void loadBlockPalette() {
        try (final InputStream inputStream = this.getClass().getClassLoader()
            .getResourceAsStream("block_palette.nbt");
             final NBTInputStream nbtInputStream = NbtUtils.createGZIPReader(inputStream)) {
            this.blockPalette = (NbtMap) nbtInputStream.readTag();

            final List<NbtMap> blocks = this.blockPalette.getList("blocks", NbtType.COMPOUND);

            final JsonObject blockPalette = new JsonObject();
            final JsonArray blocksJson = new JsonArray();
            int counter = 0;
            for (NbtMap nbtMap : blocks) {
                final int blockRuntimeId = counter++;
                final int hashedBlockRuntimeId = nbtMap.getInt("network_id");
                final Block block = new Block(blockRuntimeId, nbtMap, false);
                final Block blockHashed = new Block(hashedBlockRuntimeId, nbtMap, true);
                this.blocks.put(blockRuntimeId, block);
                this.blocks.put(hashedBlockRuntimeId, blockHashed);
                final JsonObject jsonBlock = this.createJSONBlock(nbtMap);
                this.mapping.put(nbtMap, jsonBlock);
                blocksJson.add(jsonBlock);
            }
            blockPalette.add("blocks", blocksJson);

            this.blockPaletteJson = blockPalette;
            this.proxy.getLogger().debug(
                "Loaded " + blocks.size() + " block states from the block palette"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NbtMap getBlockStateByRuntimeId(int runtimeId, boolean hashes) {
        final List<NbtMap> blocks = this.blockPalette.getList("blocks", NbtType.COMPOUND);
        for (final NbtMap nbtMap : blocks) {
            if (nbtMap.getInt("network_id") == runtimeId) {
                return nbtMap;
            }
        }
        if (!hashes && runtimeId >= 0 && runtimeId < blocks.size()) {
            return blocks.get(runtimeId);
        }
        return null;
    }

    public JsonObject getBlockStateAsJSON(NbtMap nbtMap) {
        return this.mapping.get(nbtMap);
    }

    public JsonObject createJSONBlock(NbtMap block) {
        final JsonObject blockJson = new JsonObject();
        blockJson.addProperty("name", block.getString("name"));
        final JsonArray states = new JsonArray();
        final NbtMap statesMap = block.getCompound("states");
        for (Map.Entry<String, Object> entry : statesMap.entrySet()) {
            final JsonObject state = new JsonObject();
            state.addProperty("name", entry.getKey());
            if (entry.getValue() instanceof Integer integer) {
                state.addProperty("type", "int");
                state.addProperty("value", integer);
            } else if (entry.getValue() instanceof Byte b) {
                state.addProperty("type", "byte");
                state.addProperty("value", b);
            } else if (entry.getValue() instanceof String s) {
                state.addProperty("type", "string");
                state.addProperty("value", s);
            }
            states.add(state);
        }
        blockJson.add("states", states);
        return blockJson;
    }
}