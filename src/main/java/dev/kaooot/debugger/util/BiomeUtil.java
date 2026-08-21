package dev.kaooot.debugger.util;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeCappedSurfaceData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeClimateData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeConditionalTransformationData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeConsolidatedFeatureData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeCoordinateData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeDefinitionChunkGenData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeDefinitionData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeElementData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeLegacyWorldGenRulesData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeMesaSurfaceData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeMountainParamsData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeMultinoiseGenRulesData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeNoiseGradientSurfaceData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeOverworldGenRulesData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeReplacementData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeScatterParamData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeSurfaceBuilderData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeSurfaceMaterialAdjustmentData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeSurfaceMaterialData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeWeightedData;
import org.cloudburstmc.protocol.bedrock.data.biome.BiomeWeightedTemperatureData;
import org.cloudburstmc.protocol.bedrock.data.biome.NoiseDescriptor;
import org.cloudburstmc.protocol.bedrock.data.biome.SerializedNoiseBlockSpecifier;
import org.cloudburstmc.protocol.bedrock.data.biome.VillageType;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.packet.BiomeDefinitionListPacket;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@UtilityClass
public class BiomeUtil {

    public NbtMap parseBiomeDefinitionList(BiomeDefinitionListPacket packet) {
        final List<NbtMap> parsedBiomeData = parseBiomeData(packet.getBiomes());
        return NbtMap.builder()
            .putList("biomeStringList", NbtType.STRING, packet.getBiomeStringList())
            .putList("biomeData", NbtType.COMPOUND, parsedBiomeData)
            .build();
    }

    private List<NbtMap> parseBiomeData(List<Pair<Short, BiomeDefinitionData>> biomeData) {
        final List<NbtMap> list = new ObjectArrayList<>();

        for (final Pair<Short, BiomeDefinitionData> pair : biomeData) {
            final NbtMapBuilder builder = NbtMap.builder();
            builder.putShort("index", pair.key())
                .putCompound("data", parseBiomeDefinitionData(pair.value()));

            list.add(builder.build());
        }
        return list;
    }

    private NbtMap parseBiomeDefinitionData(BiomeDefinitionData data) {
        final NbtMapBuilder builder = NbtMap.builder();

        if (data.getId() != null) {
            builder.putShort("id", data.getId());
        } else {
            // vanilla biomes do not contain ids so the server must send -1
            builder.putShort("id", (short) -1);
        }

        final List<Short> tagIds = new ObjectArrayList<>();
        if (data.getTags() != null) {
            tagIds.addAll(data.getTags());
        }

        builder.putFloat("temperature", data.getTemperature())
            .putFloat("downfall", data.getDownfall())
            .putFloat("foliageSnow", data.getFoliageSnow())
            .putFloat("depth", data.getDepth())
            .putFloat("scale", data.getScale())
            .putInt("mapWaterColorARGB", data.getMapWaterColor().getRGB())
            .putBoolean("rain", data.isRain())
            .putCompound("tags", NbtMap.builder()
                .putList("tags", NbtType.SHORT, tagIds)
                .build()
            )
            .putCompound(
                "chunkGenData", parseBiomeDefinitionChunkGenData(data.getChunkGenData())
            );
        return builder.build();
    }

    private NbtMap parseBiomeDefinitionChunkGenData(BiomeDefinitionChunkGenData chunkGenData) {
        final NbtMapBuilder builder = NbtMap.builder();

        if (chunkGenData == null) {
            return NbtMap.EMPTY;
        }

        final BiomeClimateData climate = chunkGenData.getClimate();

        if (climate != null) {
            builder.putCompound("climate", NbtMap.builder()
                .putFloat("temperature", climate.getTemperature())
                .putFloat("downfall", climate.getDownfall())
                .putFloat("snowAccumulationMin", climate.getSnowAccumulationMin())
                .putFloat("snowAccumulationMax", climate.getSnowAccumulationMax())
                .build());
        }

        final List<BiomeConsolidatedFeatureData> consolidatedFeatures =
            chunkGenData.getConsolidatedFeatures();

        if (consolidatedFeatures != null) {
            final List<NbtMap> features = new ObjectArrayList<>();
            for (final BiomeConsolidatedFeatureData feature : consolidatedFeatures) {
                final NbtMapBuilder featureBuilder = NbtMap.builder();
                final BiomeScatterParamData scatter = feature.getScatter();
                final NbtMapBuilder scatterParamBuilder = NbtMap.builder();

                final List<NbtMap> coordinateData = new ObjectArrayList<>();
                for (final BiomeCoordinateData coordinate : scatter.getCoordinates()) {
                    final NbtMapBuilder coordinateBuilder = NbtMap.builder();

                    if (coordinate.getMinValueType() != null) {
                        coordinateBuilder.putInt("minValueType",
                            coordinate.getMinValueType().ordinal());
                    }
                    coordinateBuilder.putShort("minValue", coordinate.getMinValue());
                    if (coordinate.getMaxValueType() != null) {
                        coordinateBuilder.putInt("maxValueType",
                            coordinate.getMaxValueType().ordinal());
                    }
                    coordinateBuilder.putShort("maxValue", coordinate.getMaxValue());
                    coordinateBuilder.putLong("gridOffset", coordinate.getGridOffset())
                        .putLong("gridStepSize", coordinate.getGridStepSize());
                    if (coordinate.getDistribution() != null) {
                        coordinateBuilder.putInt("distribution",
                            coordinate.getDistribution().ordinal());
                    }
                    coordinateData.add(coordinateBuilder.build());
                }

                scatterParamBuilder.putList("coordinates", NbtType.COMPOUND, coordinateData)
                    .putInt("evalOrder", scatter.getEvalOrder().ordinal())
                    .putInt("chancePercentType", scatter.getChancePercentType().ordinal())
                    .putShort("chancePercent", scatter.getChancePercent())
                    .putInt("chanceNumerator", scatter.getChanceNumerator())
                    .putInt("chanceDenominator", scatter.getChanceDenominator())
                    .putInt("iterationsType", scatter.getIterationsType().ordinal())
                    .putShort("iterations", scatter.getIterations());

                features.add(featureBuilder.putCompound("scatter", scatterParamBuilder.build())
                    .putShort("feature", feature.getFeature())
                    .putShort("identifier", feature.getIdentifier())
                    .putShort("pass", feature.getPass())
                    .putBoolean("canUseInternalFeature", feature.isCanUseInternalFeature())
                    .build());
            }

            builder.putCompound("consolidatedFeatures", NbtMap.builder()
                .putList("features", NbtType.COMPOUND, features)
                .build());
        }

        final BiomeMountainParamsData mountainParams = chunkGenData.getMountainParams();

        if (mountainParams != null) {
            builder.putCompound("mountainParams", NbtMap.builder()
                .putInt("steepBlock", mountainParams.getSteepBlock().getRuntimeId())
                .putBoolean("northSlopes", mountainParams.isNorthSlopes())
                .putBoolean("southSlopes", mountainParams.isSouthSlopes())
                .putBoolean("westSlopes", mountainParams.isWestSlopes())
                .putBoolean("eastSlopes", mountainParams.isEastSlopes())
                .putBoolean("topSlideEnabled", mountainParams.isTopSlideEnabled())
                .build());
        }

        final BiomeSurfaceMaterialAdjustmentData surfaceMaterialAdjustments =
            chunkGenData.getSurfaceMaterialAdjustment();

        if (surfaceMaterialAdjustments != null) {
            final List<NbtMap> adjustments = new ObjectArrayList<>();

            for (final BiomeElementData adjustment : surfaceMaterialAdjustments.getBiomeElements()) {
                adjustments.add(NbtMap.builder()
                    .putFloat("noiseFrequencyScale", adjustment.getNoiseFrequencyScale())
                    .putFloat("noiseLowerBound", adjustment.getNoiseLowerBound())
                    .putFloat("noiseUpperBound", adjustment.getNoiseUpperBound())
                    .putInt("heightMinType", adjustment.getHeightMinType().ordinal())
                    .putShort("heightMin", adjustment.getHeightMin())
                    .putInt("heightMaxType", adjustment.getHeightMinType().ordinal())
                    .putShort("heightMax", adjustment.getHeightMax())
                    .putCompound("adjustedMaterials",
                        parseBiomeSurfaceMaterialData(adjustment.getAdjustedMaterials()))
                    .build());
            }

            builder.putCompound("surfaceMaterialAdjustments", NbtMap.builder()
                .putList("adjustments", NbtType.COMPOUND, adjustments)
                .build());
        }

        if (chunkGenData.getSurfaceBuilderData() != null) {
            final BiomeSurfaceBuilderData surfaceBuilderData = chunkGenData.getSurfaceBuilderData();
            builder.putCompound(
                "surfaceBuilderData",
                parseBiomeSurfaceBuilderData(surfaceBuilderData, NbtMap.builder())
            );
        }

        final BiomeOverworldGenRulesData overworldGenRules = chunkGenData.getOverworldGenRules();

        if (overworldGenRules != null) {
            final NbtMapBuilder overworldGenRulesBuilder = NbtMap.builder();

            final List<NbtMap> hillsTransformations = new ObjectArrayList<>();
            for (final BiomeWeightedData transformation : overworldGenRules.getHillsTransformations()) {
                hillsTransformations.add(parseBiomeWeightedData(transformation));
            }

            final List<NbtMap> mutateTransformations = new ObjectArrayList<>();
            for (final BiomeWeightedData transformation : overworldGenRules.getMutateTransformations()) {
                mutateTransformations.add(parseBiomeWeightedData(transformation));
            }

            final List<NbtMap> riverTransformations = new ObjectArrayList<>();
            for (final BiomeWeightedData transformation : overworldGenRules.getRiverTransformations()) {
                riverTransformations.add(parseBiomeWeightedData(transformation));
            }

            final List<NbtMap> shoreTransformations = new ObjectArrayList<>();
            for (final BiomeWeightedData transformation : overworldGenRules.getShoreTransformations()) {
                shoreTransformations.add(parseBiomeWeightedData(transformation));
            }

            final List<NbtMap> preHillsEdge = new ObjectArrayList<>();
            for (final BiomeConditionalTransformationData edge : overworldGenRules.getPreHillsEdge()) {
                preHillsEdge.add(parseBiomeConditionalTransformationData(edge));
            }

            final List<NbtMap> postShoreEdge = new ObjectArrayList<>();
            for (final BiomeConditionalTransformationData edge : overworldGenRules.getPostShoreEdge()) {
                postShoreEdge.add(parseBiomeConditionalTransformationData(edge));
            }

            final List<NbtMap> climateData = new ObjectArrayList<>();
            for (final BiomeWeightedTemperatureData value : overworldGenRules.getClimate()) {
                climateData.add(NbtMap.builder()
                    .putInt("temperature", value.getTemperature().ordinal())
                    .putLong("weight", value.getWeight())
                    .build());
            }

            overworldGenRulesBuilder
                .putList("hillsTransformations", NbtType.COMPOUND, hillsTransformations)
                .putList("mutateTransformations", NbtType.COMPOUND, mutateTransformations)
                .putList("riverTransformations", NbtType.COMPOUND, riverTransformations)
                .putList("shoreTransformations", NbtType.COMPOUND, shoreTransformations)
                .putList("preHillsEdge", NbtType.COMPOUND, preHillsEdge)
                .putList("postShoreEdge", NbtType.COMPOUND, postShoreEdge)
                .putList("climate", NbtType.COMPOUND, climateData);

            builder.putCompound("overworldGenRules", overworldGenRulesBuilder.build());
        }

        final BiomeMultinoiseGenRulesData multinoiseGenRules = chunkGenData.getMultinoiseGenRules();

        if (multinoiseGenRules != null) {
            builder.putCompound("multinoiseGenRules", NbtMap.builder()
                .putFloat("temperature", multinoiseGenRules.getTemperature())
                .putFloat("humidity", multinoiseGenRules.getHumidity())
                .putFloat("altitude", multinoiseGenRules.getAltitude())
                .putFloat("weirdness", multinoiseGenRules.getWeirdness())
                .putFloat("weight", multinoiseGenRules.getWeight())
                .build());
        }

        final BiomeLegacyWorldGenRulesData legacyWorldGenRules =
            chunkGenData.getLegacyWorldGenRules();

        if (legacyWorldGenRules != null) {
            final List<NbtMap> legacyPreHills = new ObjectArrayList<>();

            for (final BiomeConditionalTransformationData value : legacyWorldGenRules.getLegacyPreHills()) {
                legacyPreHills.add(parseBiomeConditionalTransformationData(value));
            }

            builder.putCompound("legacyWorldGenRules", NbtMap.builder()
                .putList("legacyPreHills", NbtType.COMPOUND, legacyPreHills)
                .build());
        }

        final List<BiomeReplacementData> data = chunkGenData.getReplacementBiomes();
        if (data != null) {
            builder.putList("replacementBiomes", NbtType.COMPOUND, parseReplacementDataList(data));
        }

        final VillageType villageType = chunkGenData.getVillageType();
        if (villageType != null) {
            builder.putInt("villageType", villageType.ordinal());
        }

        if (chunkGenData.getSubSurfaceBuilderData() != null) {
            final BiomeSurfaceBuilderData subSurfaceBuilderData =
                chunkGenData.getSubSurfaceBuilderData();
            builder.putCompound(
                "subSurfaceBuilderData",
                parseBiomeSurfaceBuilderData(subSurfaceBuilderData, NbtMap.builder())
            );
        }
        return builder.build();
    }

    private NbtMap parseBiomeSurfaceMaterialData(BiomeSurfaceMaterialData data) {
        final NbtMapBuilder builder = NbtMap.builder();
        builder.putInt("topBlock",
            data.getTopBlock() != null ? data.getTopBlock().getRuntimeId() : -1
        );
        builder.putInt("midBlock",
            data.getMidBlock() != null ? data.getMidBlock().getRuntimeId() : -1
        );
        builder.putInt("seaFloorBlock",
            data.getSeaFloorBlock() != null ? data.getSeaFloorBlock().getRuntimeId() : -1
        );
        builder.putInt("foundationBlock",
            data.getFoundationBlock() != null ? data.getFoundationBlock().getRuntimeId() : -1
        );
        builder.putInt("seaBlock",
            data.getSeaBlock() != null ? data.getSeaBlock().getRuntimeId() : -1
        );
        builder.putInt("seaFloorDepth", data.getSeaFloorDepth());
        return builder.build();
    }

    private NbtMap parseBiomeWeightedData(BiomeWeightedData data) {
        return NbtMap.builder()
            .putShort("biomeIdentifier", data.getBiomeIdentifier())
            .putInt("weight", data.getWeight())
            .build();
    }

    private NbtMap parseBiomeConditionalTransformationData(
        BiomeConditionalTransformationData data) {
        final List<NbtMap> transformsInto = new ObjectArrayList<>();
        for (final BiomeWeightedData biomeWeightedData : data.getTransformsInto()) {
            transformsInto.add(parseBiomeWeightedData(biomeWeightedData));
        }
        return NbtMap.builder()
            .putList("transformsInto", NbtType.COMPOUND, transformsInto)
            .putShort("conditionJson", data.getConditionJson())
            .putLong("minPassingNeighbors", data.getMinPassingNeighbors())
            .build();
    }

    private List<NbtMap> parseReplacementDataList(List<BiomeReplacementData> list) {
        final List<NbtMap> replacementDataList = new ObjectArrayList<>();
        for (final BiomeReplacementData data : list) {
            replacementDataList.add(
                NbtMap.builder()
                    .putShort("replacementBiome", data.getBiome())
                    .putShort("dimension", data.getDimension())
                    .putList("targetBiomes", NbtType.SHORT, data.getTargetBiomes())
                    .putFloat("amount", data.getAmount())
                    .putFloat("noiseFrequencyScale", data.getNoiseFrequencyScale())
                    .putInt("replacementIndex", data.getReplacementIndex())
                    .build()
            );
        }
        return replacementDataList;
    }

    private NbtMap parseBiomeMesaSurfaceData(BiomeMesaSurfaceData data) {
        return NbtMap.builder()
            .putInt("clayMaterial", data.getClayMaterial().getRuntimeId())
            .putInt("hardClayMaterial", data.getHardClayMaterial().getRuntimeId())
            .putBoolean("brycePillars", data.isBrycePillars())
            .putBoolean("hasForest", data.isHasForest())
            .build();
    }

    private NbtMap parseBiomeCappedSurfaceData(BiomeCappedSurfaceData data) {
        final NbtMapBuilder cappedSurfaceBuilder = NbtMap.builder();
        final IntList floorBlocks = new IntArrayList();
        for (final BlockDefinition floorBlock : data.getFloorBlocks()) {
            floorBlocks.add(floorBlock.getRuntimeId());
        }
        final IntList ceilingBlocks = new IntArrayList();
        for (final BlockDefinition ceilingBlock : data.getCeilingBlocks()) {
            ceilingBlocks.add(ceilingBlock.getRuntimeId());
        }
        cappedSurfaceBuilder.putList("floorBlocks", NbtType.INT, floorBlocks)
            .putList("ceilingBlocks", NbtType.INT, ceilingBlocks);
        cappedSurfaceBuilder.putInt("seaBlock",
            data.getSeaBlock() != null ?
                data.getSeaBlock().getRuntimeId() : -1
        );
        cappedSurfaceBuilder.putInt("foundationBlock",
            data.getFoundationBlock() != null ?
                data.getFoundationBlock().getRuntimeId() : -1
        );
        cappedSurfaceBuilder.putInt("beachBlock",
            data.getBeachBlock() != null ?
                data.getBeachBlock().getRuntimeId() : -1
        );
        return cappedSurfaceBuilder.build();
    }

    private NbtMap parseBiomeSurfaceBuilderData(BiomeSurfaceBuilderData data,
                                                NbtMapBuilder builder) {
        final BiomeSurfaceMaterialData surfaceMaterials = data.getSurfaceMaterial();
        if (surfaceMaterials != null) {
            builder.putCompound(
                "surfaceMaterials",
                parseBiomeSurfaceMaterialData(surfaceMaterials)
            );
        }
        builder.putBoolean("hasDefaultOverworldSurface", data.isHasDefaultOverworldSurface())
            .putBoolean("hasSwampSurface", data.isHasSwampSurface())
            .putBoolean("hasFrozenOceanSurface", data.isHasFrozenOceanSurface())
            .putBoolean("hasTheEndSurface", data.isHasTheEndSurface());

        final BiomeMesaSurfaceData mesaSurface = data.getMesaSurface();
        if (mesaSurface != null) {
            builder.putCompound("mesaSurface", parseBiomeMesaSurfaceData(mesaSurface));
        }

        final BiomeCappedSurfaceData cappedSurface = data.getCappedSurface();
        if (cappedSurface != null) {
            builder.putCompound("cappedSurface", parseBiomeCappedSurfaceData(cappedSurface));
        }

        final BiomeNoiseGradientSurfaceData noiseGradientSurfaceData =
            data.getNoiseGradientSurface();
        if (noiseGradientSurfaceData != null) {
            builder.putCompound(
                "noiseGradientSurface",
                parseBiomeNoiseGradientSurfaceData(noiseGradientSurfaceData)
            );
        }
        return builder.build();
    }

    private NbtMap parseBiomeNoiseGradientSurfaceData(BiomeNoiseGradientSurfaceData data) {
        final NbtMapBuilder builder = NbtMap.builder();
        final List<Integer> nonReplaceableBlocks = new IntArrayList();
        for (final BlockDefinition block : data.getNonReplaceableBlocks()) {
            nonReplaceableBlocks.add(block.getRuntimeId());
        }
        final List<NbtMap> gradientBlocks = new ObjectArrayList<>();
        for (final SerializedNoiseBlockSpecifier specifier : data.getGradientBlocks()) {
            gradientBlocks.add(parseSerializedNoiseBlockSpecifier(specifier));
        }
        builder.putList("nonReplaceableBlocks", NbtType.INT, nonReplaceableBlocks);
        builder.putList("gradientBlocks", NbtType.COMPOUND, gradientBlocks);
        builder.putCompound("noise", parseNoiseDescriptor(data.getNoise()));
        return builder.build();
    }

    private NbtMap parseNoiseDescriptor(NoiseDescriptor descriptor) {
        return NbtMap.builder()
            .putString("name", descriptor.getName())
            .putInt("firstOctave", descriptor.getFirstOctave())
            .putList("amplitudes", NbtType.FLOAT, descriptor.getAmplitudes())
            .build();
    }

    private NbtMap parseSerializedNoiseBlockSpecifier(SerializedNoiseBlockSpecifier specifier) {
        return NbtMap.builder()
            .putString("noise", specifier.getNoise())
            .putString("threshold", specifier.getNoise())
            .putCompound("range",
                NbtMap.builder().putFloat("min", specifier.getRange().getMin())
                    .putFloat("max",
                        specifier.getRange().getMax())
                    .build())
            .putInt("block", specifier.getBlock().getRuntimeId())
            .build();
    }
}