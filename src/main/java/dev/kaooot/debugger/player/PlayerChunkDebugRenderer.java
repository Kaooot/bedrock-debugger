package dev.kaooot.debugger.player;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.shape.DebugBox;
import dev.kaooot.debugger.api.shape.DebugText;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.level.LevelChunk;
import dev.kaooot.debugger.level.LevelSubChunk;
import dev.kaooot.debugger.level.block.Block;
import dev.kaooot.debugger.util.Util;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.Dimension;
import org.cloudburstmc.protocol.bedrock.data.GeneratorType;
import org.cloudburstmc.protocol.bedrock.data.definitions.DimensionDefinition;
import org.cloudburstmc.protocol.bedrock.data.payload.common.DimensionType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class PlayerChunkDebugRenderer {

    private static final String CHUNK_POS_RENDER_SHAPE_ID = "box-chunk_outline";
    private static final String SUB_CHUNK_POS_RENDER_SHAPE_ID = "box-sub_chunk_outline";
    private static final int CHUNK_WIDTH = 16;
    private static final int CHUNK_WIDTH_HALF = CHUNK_WIDTH / 2;
    private static final int SUB_CHUNK_HEIGHT = 16;

    private final BedrockDebuggerProxy proxy;
    private final ProxiedPlayer player;

    @Getter
    private final Map<String, DebugMarkerSettings> customBlockRenderSettings =
        new Object2ObjectOpenHashMap<>();

    @Getter
    private final List<DimensionDefinition> dimensionData = new ObjectArrayList<>();

    protected DimensionDefinition getDimensionDefinition(DimensionType dimension) {
        DimensionDefinition target = null;
        for (final DimensionDefinition group : this.dimensionData) {
            if (group.getDimensionType().equals(dimension)) {
                target = group;
                break;
            }
        }
        if (target == null) {
            var data = DimensionHeightFallbackData.valueOf(dimension.asEnum().name());
            target = new DimensionDefinition(
                dimension.asEnum().name(),
                data.getMaxHeight(),
                data.getMinHeight(),
                GeneratorType.UNDEFINED,
                dimension,
                new UUID(0L, 0L),
                "minecraft:ocean"
            );
        }
        return target;
    }

    public void toggleRenderCurrentChunk(boolean newToggleValue) {
        if (this.player.getDimension().asEnum().equals(Dimension.UNDEFINED)) {
            this.proxy.getLogger().warn(
                "Failed to toggle Render Current Chunk setting because the dimension data " +
                    "for the given dimension has not been found"
            );
            return;
        }
        final DimensionDefinition target = this.player.getDimensionDefinition();
        if (newToggleValue) {
            final DebugBox chunkOutlineBox = this.createChunkRenderDebugBox(
                target.getHeightMinimum(), target.getHeightMaximum(), false
            );
            final DebugBox subChunkOutlineBox = this.createChunkRenderDebugBox(0, 0, true);

            this.proxy.getDebugShapeRenderer().renderShapes(chunkOutlineBox, subChunkOutlineBox);
        } else {
            if (this.proxy.getDebugShapeRenderer().isShapeRendered(CHUNK_POS_RENDER_SHAPE_ID)) {
                this.proxy.getDebugShapeRenderer().removeShape(CHUNK_POS_RENDER_SHAPE_ID);
            }
            if (this.proxy.getDebugShapeRenderer().isShapeRendered(SUB_CHUNK_POS_RENDER_SHAPE_ID)) {
                this.proxy.getDebugShapeRenderer().removeShape(SUB_CHUNK_POS_RENDER_SHAPE_ID);
            }
        }
    }

    public void updateChunkPosForRenderingIfEnabled(SettingsConfig config) {
        if (!config.isRenderCurrentChunk() || this.player.getDimension().asEnum().equals(Dimension.UNDEFINED)) {
            return;
        }
        final DimensionDefinition target = this.player.getDimensionDefinition();
        DebugBox chunkOutlineBox = this.proxy.getDebugShapeRenderer()
            .getShape(CHUNK_POS_RENDER_SHAPE_ID, DebugBox.class);
        DebugBox subChunkOutlineBox = this.proxy.getDebugShapeRenderer()
            .getShape(SUB_CHUNK_POS_RENDER_SHAPE_ID, DebugBox.class);
        if (chunkOutlineBox == null || subChunkOutlineBox == null) {
            chunkOutlineBox = this.createChunkRenderDebugBox(
                target.getHeightMinimum(), target.getHeightMaximum(), false
            );
            subChunkOutlineBox = this.createChunkRenderDebugBox(0, 0, true);
        } else {
            final Vector3f location = chunkOutlineBox.getLocation();
            final boolean chunkChange = this.player.getChunkX() != (location.getFloorX() >> 4) ||
                this.player.getChunkZ() != (location.getFloorZ() >> 4);
            if (chunkChange) {
                chunkOutlineBox.setLocation(
                    Vector3f.from(
                        (this.player.getChunkX() << 4) + CHUNK_WIDTH_HALF,
                        (target.getHeightMaximum() + target.getHeightMinimum()) / 2f,
                        (this.player.getChunkZ() << 4) + CHUNK_WIDTH_HALF
                    )
                );
            }
            if (chunkChange ||
                this.player.getPosition().getFloorY() >> 4 != (location.getFloorY() >> 4)) {
                subChunkOutlineBox.setLocation(
                    Vector3f.from(
                        (this.player.getChunkX() << 4) + CHUNK_WIDTH_HALF,
                        (this.ensureBoundsChunkY(target.getHeightMinimum(),
                            target.getHeightMaximum()) *
                            SUB_CHUNK_HEIGHT) + (SUB_CHUNK_HEIGHT / 2f),
                        (this.player.getChunkZ() << 4) + CHUNK_WIDTH_HALF
                    )
                );
            }
        }
        this.proxy.getDebugShapeRenderer().renderShapes(chunkOutlineBox, subChunkOutlineBox);
    }

    public void updateCustomBlockDebugMarkers(LevelChunk chunk) {
        for (final LevelSubChunk subChunk : chunk.getSubChunks()) {
            subChunk.forEachBlock(0, (localX, localY, localZ, block) -> {
                if (block.getState() == null) {
                    return;
                }
                final String name = block.getState().getString("name");
                final int blockX = (chunk.getX() << 4) + localX;
                final int blockY = (subChunk.getIndex() << 4) + localY;
                final int blockZ = (chunk.getZ() << 4) + localZ;
                if (this.customBlockRenderSettings.keySet().stream()
                    .noneMatch(id -> id.equalsIgnoreCase(name))) {
                    return;
                }
                this.updateCustomBlockDebugMarker(block, blockX, blockY, blockZ);
            });
        }
    }

    public void updateCustomBlockDebugMarker(Block block, int blockX, int blockY, int blockZ) {
        for (final String id : this.customBlockRenderSettings.keySet()) {
            if (block.getState().getString("name").equals(id)) {
                final DebugMarkerSettings settings = this.customBlockRenderSettings.get(id);
                final String textId = "debug_marker_" + id + "_" +
                    blockX + "," + blockY + "," + blockZ;

                final DebugText text = new DebugText();
                text.setId(textId);
                text.setText(id.split(":")[1]);
                text.setLocation(Vector3f.from(blockX + 0.5f, blockY + 1, blockZ + 0.5f));
                text.setDimension(this.proxy.getPlayer().getDimension());
                text.setColor(settings.getTextColor());
                text.setBackgroundColor(settings.getTextBackgroundColor());
                text.setScale(1.5f);

                this.proxy.getDebugShapeRenderer().renderShape(text);
                break;
            }
        }
    }

    private DebugBox createChunkRenderDebugBox(int heightMax, int heightMin, boolean subChunk) {
        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);

        final DebugBox box = new DebugBox();
        box.setId(subChunk ? SUB_CHUNK_POS_RENDER_SHAPE_ID : CHUNK_POS_RENDER_SHAPE_ID);
        box.setLocation(
            Vector3f.from(
                (this.player.getChunkX() << 4) + CHUNK_WIDTH_HALF,
                subChunk ?
                    (this.ensureBoundsChunkY(heightMin, heightMax) * SUB_CHUNK_HEIGHT) +
                        (SUB_CHUNK_HEIGHT / 2f) :
                    (Math.abs(heightMin) + Math.abs(heightMax)) / 2f,
                (this.player.getChunkZ() << 4) + CHUNK_WIDTH_HALF
            )
        );
        box.setBoxBound(
            Vector3f.from(
                CHUNK_WIDTH - (subChunk ? 0.01f : 0),
                subChunk ? SUB_CHUNK_HEIGHT : Math.abs(heightMax) + Math.abs(heightMin),
                CHUNK_WIDTH - (subChunk ? 0.01f : 0))
        );
        box.setColor(subChunk ? Util.rgbToColor(
                settingsConfig.getSubChunkDebugRendererColorR(),
                settingsConfig.getSubChunkDebugRendererColorG(),
                settingsConfig.getSubChunkDebugRendererColorB()
            ) : Util.rgbToColor(
                settingsConfig.getChunkDebugRendererColorR(),
                settingsConfig.getChunkDebugRendererColorG(),
                settingsConfig.getChunkDebugRendererColorB()
            )
        );
        return box;
    }

    private int ensureBoundsChunkY(int heightMin, int heightMax) {
        return (Math.min(Math.max(this.player.getBlockBelow().getY(), heightMin),
            heightMax - 1) >> 4);
    }


    @Getter
    @RequiredArgsConstructor
    private enum DimensionHeightFallbackData {
        OVERWORLD(-64, 320),
        NETHER(0, 128),
        THE_END(0, 256);

        private final int minHeight;
        private final int maxHeight;
    }
}