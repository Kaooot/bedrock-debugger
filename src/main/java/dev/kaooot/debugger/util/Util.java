package dev.kaooot.debugger.util;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import lombok.experimental.UtilityClass;
import org.cloudburstmc.nbt.NBTOutputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.NbtUtils;
import org.cloudburstmc.protocol.bedrock.data.ControlScheme;
import org.cloudburstmc.protocol.bedrock.data.TrimMaterial;
import org.cloudburstmc.protocol.bedrock.data.TrimPattern;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraAimAssistCategory;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraAimAssistItemSettings;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraAimAssistPreset;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraAimAssistPresetDefinition;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraAimAssistPriority;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraPreset;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import dev.kaooot.debugger.BedrockDebuggerProxy;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@UtilityClass
public class Util {

    public final Converter<String, String> CONVERTER = CaseFormat.UPPER_UNDERSCORE
        .converterTo(CaseFormat.UPPER_CAMEL);

    public int indexOf(int x, int y, int z) {
        return ((x & 15) << 8) + ((z & 15) << 4) + (y & 15);
    }

    public long hash(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | ((long) chunkZ & 0xffffffffL);
    }

    public int rgbToColor(int r, int g, int b) {
        return rgbToColor(r, g, b, 255);
    }

    public int rgbToColor(int r, int g, int b, int alpha) {
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    public String round(float value) {
        return round(value, 2);
    }

    public String round(float value, int decimalPlaces) {
        return String.format(Locale.US, "%." + decimalPlaces + "f", value);
    }

    public JsonObject convertCompoundToJson(NbtMap compound) {
        final JsonObject jsonObject = new JsonObject();
        for (final String s : compound.keySet()) {
            compound.listenForByte(s, value -> jsonObject.addProperty(s, value == 1));
            compound.listenForNumber(s, number -> {
                if (!(number instanceof Byte)) {
                    jsonObject.addProperty(s, number);
                }
            });
            compound.listenForCompound(s, value -> jsonObject.add(s,
                convertCompoundToJson(value)));
            compound.listenForList(s, NbtType.BYTE, value -> jsonObject.add(s,
                convertListToJson(value)));
            compound.listenForList(s, NbtType.SHORT, value -> jsonObject.add(s,
                convertListToJson(value)));
            compound.listenForList(s, NbtType.INT, value -> jsonObject.add(s,
                convertListToJson(value)));
            compound.listenForList(s, NbtType.LONG, value -> jsonObject.add(s,
                convertListToJson(value)));
            compound.listenForList(s, NbtType.FLOAT, value -> jsonObject.add(s,
                convertListToJson(value)));
            compound.listenForList(s, NbtType.DOUBLE, value -> jsonObject.add(s,
                convertListToJson(value)));
            compound.listenForList(s, NbtType.STRING, value -> jsonObject.add(s,
                convertListToJson(value)));
            compound.listenForList(s, NbtType.LIST, value -> jsonObject.add(s,
                convertListToJson(value)));
            compound.listenForList(s, NbtType.COMPOUND, value -> jsonObject.add(s,
                convertListToJson(value)));
            compound.listenForString(s, value -> jsonObject.addProperty(s, value));
        }
        return jsonObject;
    }

    public JsonArray convertListToJson(List<?> list) {
        final JsonArray jsonArray = new JsonArray();
        for (final Object o : list) {
            if (o instanceof Number number) {
                jsonArray.add(number);
            } else if (o instanceof NbtMap compound) {
                jsonArray.add(convertCompoundToJson(compound));
            } else {
                jsonArray.add(o.toString());
            }
        }
        return jsonArray;
    }

    public void dumpPaletteNbt(NbtMap nbtMap, File file) {
        try (final FileOutputStream outputStream = new FileOutputStream(file);
             final NBTOutputStream nbtOutputStream = NbtUtils.createGZIPWriter(outputStream)) {
            nbtOutputStream.writeTag(nbtMap);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public void dumpPaletteJson(NbtMap nbtMap, BedrockDebuggerProxy proxy, File file) {
        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(proxy.getGson().toJson(Util.convertCompoundToJson(nbtMap))
                .getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public NbtMap convertItemDefinitionsToNbt(List<ItemDefinition> definitions) {
        final List<NbtMap> list = new ObjectArrayList<>();
        for (final ItemDefinition itemDefinition : definitions) {
            final SimpleItemDefinition definition = (SimpleItemDefinition) itemDefinition;
            list.add(NbtMap.builder()
                .putString("name", definition.getIdentifier())
                .putInt("id", definition.getRuntimeId())
                .putInt("version", definition.getVersion().ordinal())
                .putBoolean("component_based", definition.isComponentBased())
                .build());
        }
        return NbtMap.builder()
            .putList("items", NbtType.COMPOUND, list)
            .build();
    }

    public NbtMap convertTrimDataToNbt(List<TrimPattern> patterns, List<TrimMaterial> materials) {
        final List<NbtMap> trimPatternsList = new ObjectArrayList<>();
        final List<NbtMap> trimMaterialsList = new ObjectArrayList<>();
        for (final TrimPattern trimPattern : patterns) {
            trimPatternsList.add(NbtMap.builder()
                .putString("itemName", trimPattern.getItemName())
                .putString("patternId", trimPattern.getPatternId())
                .build());
        }
        for (final TrimMaterial trimMaterial : materials) {
            trimMaterialsList.add(NbtMap.builder()
                .putString("materialId", trimMaterial.getMaterialId())
                .putString("color", trimMaterial.getColor())
                .putString("itemName", trimMaterial.getItemName())
                .build());
        }
        return NbtMap.builder()
            .putList("patterns", NbtType.COMPOUND, trimPatternsList)
            .putList("materials", NbtType.COMPOUND, trimMaterialsList)
            .build();
    }

    public NbtMap convertCameraPresetsToNbt(List<CameraPreset> presets) {
        final List<NbtMap> cameraPresets = new ObjectArrayList<>();
        for (final CameraPreset preset : presets) {
            final NbtMapBuilder builder = NbtMap.builder().putString("name", preset.getName());
            if (!preset.getInheritFrom().isEmpty()) {
                builder.putString("inheritFrom", preset.getInheritFrom());
            }
            if (preset.getPos() != null) {
                builder.putCompound("pos", NbtMap.builder()
                    .putFloat("x", preset.getPos().getX())
                    .putFloat("y", preset.getPos().getY())
                    .putFloat("z", preset.getPos().getZ())
                    .build());
            }
            if (preset.getYaw() != null) {
                builder.putFloat("yaw", preset.getYaw());
            }
            if (preset.getPitch() != null) {
                builder.putFloat("pitch", preset.getPitch());
            }
            if (preset.getRotationSpeed() != null) {
                builder.putFloat("rotationSpeed", preset.getRotationSpeed());
            }
            if (preset.getSnapToTarget() != null &&
                preset.getSnapToTarget().isPresent()) {
                builder.putBoolean("snapToTarget", preset.getSnapToTarget().getAsBoolean());
            }
            if (preset.getHorizontalRotationLimit() != null) {
                builder.putCompound("horizontalRotationLimit", NbtMap.builder()
                    .putFloat("x", preset.getHorizontalRotationLimit().getX())
                    .putFloat("y", preset.getHorizontalRotationLimit().getY())
                    .build());
            }
            if (preset.getVerticalRotationLimit() != null) {
                builder.putCompound("verticalRotationLimit", NbtMap.builder()
                    .putFloat("x", preset.getHorizontalRotationLimit().getX())
                    .putFloat("y", preset.getHorizontalRotationLimit().getY())
                    .build());
            }
            if (preset.getContinueTargeting() != null &&
                preset.getContinueTargeting().isPresent()) {
                builder.putBoolean("continueTargeting", preset.getContinueTargeting()
                    .getAsBoolean());
            }
            if (preset.getBlockListeningRadius() != null) {
                builder.putFloat("blockListeningRadius", preset.getBlockListeningRadius());
            }
            if (preset.getViewOffset() != null) {
                builder.putCompound("viewOffset", NbtMap.builder()
                    .putFloat("x", preset.getViewOffset().getX())
                    .putFloat("y", preset.getViewOffset().getY())
                    .build());
            }
            if (preset.getEntityOffset() != null) {
                builder.putCompound("entityOffset", NbtMap.builder()
                    .putFloat("x", preset.getEntityOffset().getX())
                    .putFloat("y", preset.getEntityOffset().getY())
                    .putFloat("z", preset.getEntityOffset().getZ())
                    .build());
            }
            if (preset.getRadius() != null) {
                builder.putFloat("radius", preset.getRadius());
            }
            if (preset.getYawLimitMin() != null) {
                builder.putFloat("yawLimitMin", preset.getYawLimitMin());
            }
            if (preset.getYawLimitMax() != null) {
                builder.putFloat("yawLimitMax", preset.getYawLimitMax());
            }
            if (preset.getListener() != null) {
                builder.putString("listener", preset.getListener().name());
            }
            if (preset.getPlayerEffects() != null && preset.getPlayerEffects().isPresent()) {
                builder.putBoolean("playerEffects", preset.getPlayerEffects().getAsBoolean());
            }
            if (preset.getAlignTargetAndCameraForward() != null &&
                preset.getAlignTargetAndCameraForward().isPresent()) {
                builder.putBoolean("alignTargetAndCameraForward",
                    preset.getAlignTargetAndCameraForward().getAsBoolean());
            }
            final CameraAimAssistPreset aimAssist = preset.getAimAssist();
            if (aimAssist != null) {
                final NbtMapBuilder b = NbtMap.builder();

                if (aimAssist.getPresetId() != null) {
                    b.putString("presetId", aimAssist.getPresetId());
                }
                if (aimAssist.getTargetMode() != null) {
                    b.putInt("targetMode", aimAssist.getTargetMode());
                }
                if (aimAssist.getViewAngle() != null) {
                    b.putCompound("viewAngle", NbtMap.builder()
                        .putFloat("x", aimAssist.getViewAngle().getX())
                        .putFloat("y", aimAssist.getViewAngle().getY())
                        .build());
                }
                if (aimAssist.getDistance() != null) {
                    b.putFloat("distance", aimAssist.getDistance());
                }

                builder.putCompound("aimAssist", b.build());
            }
            final ControlScheme controlScheme = preset.getControlScheme();
            if (controlScheme != null) {
                builder.putString("controlScheme", controlScheme.name());
            }

            cameraPresets.add(builder.build());
        }
        return NbtMap.builder().putList("presets", NbtType.COMPOUND, cameraPresets).build();
    }

    public List<NbtMap> convertCameraAimAssistCategoriesToNbt(
        List<CameraAimAssistCategory> categories) {
        final List<NbtMap> categoriesList = new ObjectArrayList<>();
        for (final CameraAimAssistCategory category : categories) {
            final NbtMapBuilder categoryBuilder = NbtMap.builder()
                .putString("name", category.getName());

            final List<NbtMap> entities = new ObjectArrayList<>();
            for (final CameraAimAssistPriority entity : category.getEntities()) {
                entities.add(NbtMap.builder().putString("itemId", entity.getId())
                    .putInt("priority", entity.getPriority())
                    .build());
            }

            final List<NbtMap> blocks = new ObjectArrayList<>();
            for (final CameraAimAssistPriority block : category.getBlocks()) {
                blocks.add(NbtMap.builder().putString("blockId", block.getId())
                    .putInt("priority", block.getPriority())
                    .build());
            }

            final NbtMapBuilder prioritiesBuilder = NbtMap.builder()
                .putList("entities", NbtType.COMPOUND, entities)
                .putList("blocks", NbtType.COMPOUND, blocks);

            if (category.getEntityDefault() != null) {
                prioritiesBuilder.putInt("entityDefault", category.getEntityDefault());
            }

            if (category.getBlockDefault() != null) {
                prioritiesBuilder.putInt("blockDefault", category.getBlockDefault());
            }

            categoryBuilder.putCompound("priorities", prioritiesBuilder.build());

            categoriesList.add(categoryBuilder.build());
        }
        return categoriesList;
    }

    public List<NbtMap> convertCameraAimAssistPresetsToNbt(
        List<CameraAimAssistPresetDefinition> presets) {
        final List<NbtMap> presetsList = new ObjectArrayList<>();
        for (final CameraAimAssistPresetDefinition preset : presets) {
            final NbtMapBuilder builder = NbtMap.builder()
                .putString("identifier", preset.getIdentifier())
                .putList("exclusionList", NbtType.STRING, preset.getExclusionList())
                .putList("liquidTargetingList", NbtType.STRING, preset.getLiquidTargetingList());

            final List<NbtMap> itemSettingsList = new ObjectArrayList<>();
            for (final CameraAimAssistItemSettings itemSetting : preset.getItemSettings()) {
                itemSettingsList.add(NbtMap.builder()
                    .putString("itemId", itemSetting.getItemId())
                    .putString("category", itemSetting.getCategory())
                    .build());
            }

            builder.putList("itemSettings", NbtType.COMPOUND, itemSettingsList);

            if (preset.getDefaultItemSettings() != null) {
                builder.putString("defaultItemSettings", preset.getDefaultItemSettings());
            }

            if (preset.getHandSettings() != null) {
                builder.putString("handSettings", preset.getHandSettings());
            }

            presetsList.add(builder.build());
        }
        return presetsList;
    }

    public String nbtToBase64(NbtMap nbtMap) {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             final NBTOutputStream nbtOutputStream = NbtUtils.createWriterLE(outputStream)) {
            nbtOutputStream.writeTag(nbtMap);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to represent nbt as base64: " + nbtMap);
    }
}