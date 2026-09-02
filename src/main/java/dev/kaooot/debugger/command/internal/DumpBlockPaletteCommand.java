package dev.kaooot.debugger.command.internal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.Command;
import dev.kaooot.debugger.api.command.annotation.Description;
import dev.kaooot.debugger.api.command.annotation.Name;
import dev.kaooot.debugger.core.blockpalette.BlockPaletteExtractor;
import dev.kaooot.debugger.core.blockpalette.BlockPaletteReader.PaletteEntry;
import dev.kaooot.debugger.core.blockpalette.BlockStateReader;
import dev.kaooot.debugger.util.BlockUtil;
import dev.kaooot.debugger.util.DebugServerHelper;
import dev.kaooot.debugger.util.Util;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Name("dumpblockpalette")
@Description("Outputs valid block nbt in a binary format.")
public class DumpBlockPaletteCommand extends Command<BedrockDebuggerProxy> {

    private static final long GRACE_MILLIS = 15_000;
    private static final long TIMEOUT_MILLIS = 90_000;
    private static final long RETRY_MILLIS = 4_000;

    private volatile boolean running;

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        if (this.running) {
            proxy.getPlayer().sendMessage("§cFailure! :( Already running.");
            return;
        }
        this.running = true;

        final DebugServerHelper helper = new DebugServerHelper(proxy);
        helper.startDebugServer().whenComplete((result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                proxy.getPlayer().sendMessage("§cFailure :( Error: " + throwable.getMessage());
                this.running = false;
                return;
            }
            CompletableFuture.runAsync(() -> this.dump(helper, proxy));
        });
    }

    private void dump(DebugServerHelper helper, BedrockDebuggerProxy proxy) {
        try {
            final long pid = helper.getProcessId();
            if (pid < 0) {
                proxy.getPlayer().sendMessage("§cFailure :( The debug server did not start.");
                return;
            }

            final List<PaletteEntry> palette = BlockPaletteExtractor.extractWithRetry(
                pid, GRACE_MILLIS, TIMEOUT_MILLIS, RETRY_MILLIS
            );
            if (palette == null || palette.isEmpty()) {
                proxy.getPlayer().sendMessage(
                    "§cFailure :( Could not dump the block palette from pid " + pid
                );
                return;
            }

            final File nbtFile = new File(proxy.getDataLogsFolder(), "block_palette.nbt");
            final File jsonFile = new File(proxy.getDataLogsFolder(), "block_palette.json");

            this.writeNbt(palette, nbtFile);
            this.writeJson(palette, jsonFile, proxy.getGson());

            proxy.getPlayer().sendMessage(
                "Success! :) File output to: " + jsonFile.getAbsolutePath()
            );
        } catch (Exception e) {
            e.printStackTrace();
            proxy.getPlayer().sendMessage("§cFailure :( Error: " + e.getMessage());
        } finally {
            helper.stopDebugServer();
            this.running = false;
        }
    }

    private NbtMap toNbt(final List<PaletteEntry> palette) {
        final List<NbtMap> blocks = new ArrayList<>(palette.size());
        for (final PaletteEntry entry : palette) {
            final NbtMapBuilder states = NbtMap.builder();
            for (final BlockStateReader.StateProperty property : entry.getStates()) {
                switch (property.getType()) {
                    case "int" -> states.putInt(property.getName(), (Integer) property.getValue());
                    case "byte" -> states.putByte(property.getName(), (Byte) property.getValue());
                    default -> states.putString(property.getName(), (String) property.getValue());
                }
            }
            final NbtMap statesMap = states.build();
            blocks.add(NbtMap.builder()
                .putInt("network_id", BlockUtil.createHash(entry.getName(), statesMap))
                .putLong("name_hash", entry.getNameHash())
                .putString("name", entry.getName())
                .putInt("version", entry.getVersion())
                .putCompound("states", statesMap)
                .build());
        }
        return NbtMap.builder().putList("blocks", NbtType.COMPOUND, blocks).build();
    }

    private JsonArray toJson(final List<PaletteEntry> palette) {
        final JsonArray array = new JsonArray(palette.size());
        for (final PaletteEntry entry : palette) {
            final JsonObject block = new JsonObject();
            block.addProperty("name", entry.getName());
            final JsonArray states = new JsonArray();
            for (final BlockStateReader.StateProperty property : entry.getStates()) {
                final JsonObject state = new JsonObject();
                state.addProperty("name", property.getName());
                state.addProperty("type", property.getType());
                switch (property.getType()) {
                    case "int" -> state.addProperty("value", (Integer) property.getValue());
                    case "byte" ->
                        state.addProperty("value", ((Byte) property.getValue()).intValue());
                    default -> state.addProperty("value", (String) property.getValue());
                }
                states.add(state);
            }
            block.add("states", states);
            array.add(block);
        }
        return array;
    }

    private void writeNbt(final List<PaletteEntry> palette, final File file) {
        final File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        Util.dumpPaletteNbt(toNbt(palette), file);
    }

    private void writeJson(final List<PaletteEntry> palette, final File file, Gson gson)
        throws IOException {
        final File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        Files.writeString(
            file.toPath(),
            gson.toJson(toJson(palette)),
            StandardCharsets.UTF_8
        );
    }
}