package dev.kaooot.debugger.util;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Value;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.descriptor.ComplexAliasDescriptor;
import org.cloudburstmc.protocol.bedrock.data.inventory.descriptor.DeferredDescriptor;
import org.cloudburstmc.protocol.bedrock.data.inventory.descriptor.ItemDescriptor;
import org.cloudburstmc.protocol.bedrock.data.inventory.descriptor.ItemDescriptorType;
import org.cloudburstmc.protocol.bedrock.data.inventory.descriptor.ItemTagDescriptor;
import org.cloudburstmc.protocol.bedrock.data.inventory.descriptor.MolangDescriptor;
import org.cloudburstmc.protocol.bedrock.data.inventory.descriptor.NameDescriptor;
import org.cloudburstmc.protocol.bedrock.data.inventory.descriptor.RecipeIngredient;
import org.cloudburstmc.protocol.bedrock.data.payload.crafting.MaterialReducerEntryOutput;
import org.cloudburstmc.protocol.bedrock.data.payload.crafting.MultiRecipePayload;
import org.cloudburstmc.protocol.bedrock.data.payload.crafting.ShapedRecipePayload;
import org.cloudburstmc.protocol.bedrock.data.payload.crafting.ShapelessRecipePayload;
import org.cloudburstmc.protocol.bedrock.data.payload.crafting.SmithingTransformRecipePayload;
import org.cloudburstmc.protocol.bedrock.data.payload.crafting.SmithingTrimRecipePayload;
import org.cloudburstmc.protocol.bedrock.packet.CraftingDataPacket;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.NetworkConstants;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class RecipeUtil {

    private final List<Character> shapeChars = Arrays.asList(
        'A', 'B',
        'C', 'D',
        'E', 'F',
        'G', 'H',
        'I', 'J'
    );

    public NbtMap parseRecipes(CraftingDataPacket packet, BedrockDebuggerProxy proxy) {
        final List<CraftingDataEntry> craftingData = new ObjectArrayList<>();
        final List<PotionMixDataEntry> potionMixes = new ObjectArrayList<>();
        final List<ContainerMixDataEntry> containerMixes = new ObjectArrayList<>();
        final List<MaterialReducerDataEntry> materialReducers = new ObjectArrayList<>();
        final List<ItemDefinition> definitions = proxy.getPlayer().getItemDefinitions();

        for (final ShapedRecipePayload payload : packet.getShapedRecipes()) {
            craftingData.add(this.parseShapedRecipe(0, payload, proxy));
        }

        for (final ShapelessRecipePayload payload : packet.getShapelessRecipes()) {
            craftingData.add(this.parseShapelessRecipe(1, payload, proxy));
        }

        for (final MultiRecipePayload payload : packet.getMultiRecipes()) {
            craftingData.add(
                new CraftingDataEntry(
                    null,
                    2,
                    null,
                    null,
                    null,
                    null,
                    payload.getMultiRecipeUUID(),
                    payload.getNetId().getRawId(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            );
        }

        for (final ShapelessRecipePayload payload : packet.getUserDataShapelessRecipes()) {
            craftingData.add(this.parseShapelessRecipe(3, payload, proxy));
        }

        for (final ShapelessRecipePayload payload : packet.getShapelessChemistryRecipes()) {
            craftingData.add(this.parseShapelessRecipe(4, payload, proxy));
        }

        for (final ShapedRecipePayload payload : packet.getShapedChemistryRecipes()) {
            craftingData.add(this.parseShapedRecipe(5, payload, proxy));
        }

        for (final SmithingTransformRecipePayload payload : packet.getSmithingTransformRecipes()) {
            craftingData.add(
                new CraftingDataEntry(
                    payload.getRecipeId(),
                    6,
                    null,
                    null,
                    null,
                    null,
                    null,
                    payload.getNetId().getRawId(),
                    null,
                    this.fromNetwork(payload.getBaseIngredient(), proxy),
                    this.fromNetwork(payload.getAdditionIngredient(), proxy),
                    this.fromNetwork(payload.getTemplateIngredient(), proxy),
                    this.fromNetwork(payload.getResult()),
                    null,
                    null,
                    null
                )
            );
        }

        for (final SmithingTrimRecipePayload payload : packet.getSmithingTrimRecipes()) {
            craftingData.add(
                new CraftingDataEntry(
                    payload.getRecipeId(),
                    7,
                    null,
                    null,
                    null,
                    null,
                    null,
                    payload.getNetId().getRawId(),
                    null,
                    this.fromNetwork(payload.getBaseIngredient(), proxy),
                    this.fromNetwork(payload.getAdditionIngredient(), proxy),
                    this.fromNetwork(payload.getTemplateIngredient(), proxy),
                    null,
                    null,
                    null,
                    null
                )
            );
        }

        for (final org.cloudburstmc.protocol.bedrock.data.payload.crafting.PotionMixDataEntry potionMix : packet.getPotionMixes()) {
            potionMixes.add(
                new PotionMixDataEntry(
                    getItemIdByRuntime(potionMix.getFromPotionId(), definitions),
                    potionMix.getFromItemAux(),
                    getItemIdByRuntime(potionMix.getReagentItemId(), definitions),
                    potionMix.getReagentItemAux(),
                    getItemIdByRuntime(potionMix.getToPotionId(), definitions),
                    potionMix.getToItemAux()
                )
            );
        }

        for (final org.cloudburstmc.protocol.bedrock.data.payload.crafting.ContainerMixDataEntry containerMix : packet.getContainerMixes()) {
            containerMixes.add(new ContainerMixDataEntry(
                    getItemIdByRuntime(containerMix.getFromItemId(), definitions),
                    getItemIdByRuntime(containerMix.getReagentItemId(), definitions),
                    getItemIdByRuntime(containerMix.getOutputItemId(), definitions)
                )
            );
        }

        for (final org.cloudburstmc.protocol.bedrock.data.payload.crafting.MaterialReducerDataEntry materialReducer : packet.getMaterialReducers()) {
            final List<RecipeItemDefinitionEntry> itemCounts = new ObjectArrayList<>();
            for (final MaterialReducerEntryOutput output : materialReducer.getItemIdsAndCounts()) {
                itemCounts.add(
                    new RecipeItemDefinitionEntry(
                        this.getItemIdByRuntime(output.getItemId(), definitions),
                        output.getItemCount()
                    )
                );
            }
            materialReducers.add(
                new MaterialReducerDataEntry(
                    materialReducer.getFromItemKey(),
                    itemCounts
                )
            );
        }
        return new Recipes(NetworkConstants.CODEC.getProtocolVersion(), craftingData, potionMixes,
            containerMixes, materialReducers).toNbt();
    }

    private CraftingDataEntry parseShapedRecipe(int type, ShapedRecipePayload payload,
                                                BedrockDebuggerProxy proxy) {

        int charCounter = 0;
        final int width = payload.getWidth();
        final int height = payload.getHeight();
        final List<RecipeIngredient> inputs = payload.getIngredients();
        final Map<RecipeItemDescriptor, Character> charItemMap = new HashMap<>();
        final char[][] shape = new char[height][width];
        final String[] entryShape;
        for (int i = 0; i < height; i++) {
            Arrays.fill(shape[i], ' ');

            final int index = i * width;

            for (int j = 0; j < width; j++) {
                final int slot = index + j;
                final RecipeItemDescriptor descriptor = this.fromNetwork(inputs.get(slot),
                    proxy);

                if (descriptor.getType().equalsIgnoreCase(ItemDescriptorType.EMPTY.name()
                    .toLowerCase())) {
                    continue;
                }

                Character shapeChar = charItemMap.get(descriptor);

                if (shapeChar == null) {
                    shapeChar = this.shapeChars.get(charCounter++);

                    charItemMap.put(descriptor, shapeChar);
                }
                shape[i][j] = shapeChar;
            }
        }

        final List<String> shapes = new ObjectArrayList<>();

        for (final char[] chars : shape) {
            shapes.add(String.valueOf(chars));
        }

        entryShape = shapes.toArray(String[]::new);

        final Map<Character, RecipeItemDescriptor> input = new HashMap<>();

        for (final Map.Entry<RecipeItemDescriptor, Character> entry : charItemMap.entrySet()) {
            input.put(entry.getValue(), entry.getKey());
        }

        UnlockingRequirement requirement = null;
        if (!payload.getUnlockingRequirement().isInvalid()) {
            final List<RecipeItemDescriptor> ingredients = new ObjectArrayList<>();
            for (final RecipeIngredient ingredient : payload.getUnlockingRequirement()
                .getUnlockingIngredients()) {
                ingredients.add(this.fromNetwork(ingredient, proxy));
            }
            requirement = new UnlockingRequirement(
                payload.getUnlockingRequirement().getUnlockingContext()
                    .ordinal(), ingredients
            );
        }
        return new CraftingDataEntry(
            payload.getRecipeId(),
            type,
            input,
            this.writeRecipeItems(payload.getResults()),
            entryShape,
            payload.getTag(),
            payload.getUuid(),
            payload.getNetId().getRawId(),
            payload.getPriority(),
            null,
            null,
            null,
            null,
            payload.getWidth(),
            payload.getHeight(),
            requirement
        );
    }

    private CraftingDataEntry parseShapelessRecipe(int type, ShapelessRecipePayload payload,
                                                   BedrockDebuggerProxy proxy) {
        UnlockingRequirement requirement = null;
        if (!payload.getUnlockingRequirement().isInvalid()) {
            final List<RecipeItemDescriptor> ingredients = new ObjectArrayList<>();
            for (final RecipeIngredient ingredient : payload.getUnlockingRequirement()
                .getUnlockingIngredients()) {
                ingredients.add(this.fromNetwork(ingredient, proxy));
            }
            requirement = new UnlockingRequirement(
                payload.getUnlockingRequirement().getUnlockingContext()
                    .ordinal(), ingredients
            );
        }
        return new CraftingDataEntry(
            payload.getRecipeId(),
            type,
            this.writeRecipeItemDescriptors(payload.getIngredients(), proxy),
            this.writeRecipeItems(payload.getResults()),
            null,
            payload.getTag(),
            payload.getUuid(),
            payload.getNetId().getRawId(),
            payload.getPriority(),
            null,
            null,
            null,
            null,
            null,
            null,
            requirement
        );
    }

    private List<RecipeItem> writeRecipeItems(List<ItemData> inputs) {
        final List<RecipeItem> outputs = new ObjectArrayList<>();

        for (final ItemData input : inputs) {
            final RecipeItem item = this.fromNetwork(input);

            if (!item.getId().equalsIgnoreCase("minecraft:air")) {
                outputs.add(item);
            }
        }
        return outputs;
    }

    private List<RecipeItemDescriptor> writeRecipeItemDescriptors(
        List<RecipeIngredient> inputs, BedrockDebuggerProxy proxy) {
        final List<RecipeItemDescriptor> outputs = new ObjectArrayList<>();

        for (final RecipeIngredient input : inputs) {
            final RecipeItemDescriptor descriptor = this.fromNetwork(input, proxy);

            if (!descriptor.getType().equals(ItemDescriptorType.EMPTY.name().toLowerCase())) {
                outputs.add(descriptor);
            }
        }
        return outputs;
    }

    private RecipeItem fromNetwork(ItemData itemData) {
        final String id = itemData.getDefinition().getIdentifier();
        final int count = itemData.getCount();
        Integer damage = itemData.getDamage();
        String tag = null;

        if (itemData.getTag() != null) {
            tag = Util.nbtToBase64(itemData.getTag());
        }

        if (damage == 0 || damage == -1) {
            damage = null;
        }
        return new RecipeItem(id, count, damage, tag);
    }

    private RecipeItemDescriptor fromNetwork(RecipeIngredient descriptor,
                                             BedrockDebuggerProxy proxy) {
        final ItemDescriptor itemDescriptor = descriptor.getDescriptor();
        String name = null;
        String itemId = null;
        Integer auxValue = null;
        String fullName = null;
        String itemTag = null;
        String tagExpression = null;
        Integer molangVersion = null;

        switch (itemDescriptor.getType()) {
            case COMPLEX_ALIAS -> name = ((ComplexAliasDescriptor) itemDescriptor).getName();
            case NAME -> {
                itemId = this.getItemIdByRuntime(((NameDescriptor) itemDescriptor).getItemId()
                    .getRuntimeId(), proxy.getPlayer().getItemDefinitions());
                if (itemId == null) {
                    throw new RuntimeException("Failed to find identifier by legacy id");
                }
                auxValue = ((NameDescriptor) itemDescriptor).getAuxValue();
            }
            case DEFERRED -> fullName = ((DeferredDescriptor) itemDescriptor).getFullName();
            case ITEM_TAG -> itemTag = ((ItemTagDescriptor) itemDescriptor).getItemTag();
            case MOLANG -> {
                tagExpression = ((MolangDescriptor) itemDescriptor).getTagExpression();
                molangVersion = ((MolangDescriptor) itemDescriptor).getMolangVersion();
            }
        }
        return new RecipeItemDescriptor(itemDescriptor.getType().name().toLowerCase(),
            descriptor.getStackSize(), name, itemId, auxValue, fullName,
            itemTag, tagExpression, molangVersion);
    }

    private String getItemIdByRuntime(int runtimeId, List<ItemDefinition> definitions) {
        for (final ItemDefinition itemDefinition : definitions) {
            if (itemDefinition.getRuntimeId() == runtimeId) {
                return itemDefinition.getIdentifier();
            }
        }
        return null;
    }

    @Value
    private static class Recipes {
        int version;
        List<CraftingDataEntry> recipes;
        List<RecipeUtil.PotionMixDataEntry> potionMixes;
        List<RecipeUtil.ContainerMixDataEntry> containerMixes;
        List<RecipeUtil.MaterialReducerDataEntry> materialReducers;

        public NbtMap toNbt() {
            final NbtMapBuilder builder = NbtMap.builder();
            builder.putInt("version", this.version);

            final List<NbtMap> recipes = new ObjectArrayList<>();

            for (final CraftingDataEntry recipe : this.recipes) {
                recipes.add(recipe.toNbt());
            }

            builder.putList("recipes", NbtType.COMPOUND, recipes);

            final List<NbtMap> potionMixes = new ObjectArrayList<>();

            for (final RecipeUtil.PotionMixDataEntry potionMix : this.potionMixes) {
                potionMixes.add(potionMix.toNbt());
            }

            builder.putList("potionMixes", NbtType.COMPOUND, potionMixes);

            final List<NbtMap> containerMixes = new ObjectArrayList<>();

            for (final RecipeUtil.ContainerMixDataEntry containerMix : this.containerMixes) {
                containerMixes.add(containerMix.toNbt());
            }

            builder.putList("containerMixes", NbtType.COMPOUND, containerMixes);

            if (!this.materialReducers.isEmpty()) {
                final List<NbtMap> materialReducers = new ObjectArrayList<>();

                for (final RecipeUtil.MaterialReducerDataEntry materialReducer : this.materialReducers) {
                    materialReducers.add(materialReducer.toNbt());
                }

                builder.putList("materialReducers", NbtType.COMPOUND, materialReducers);
            }
            return builder.build();
        }
    }

    @Value
    private static class PotionMixDataEntry {
        String inputId;
        int inputMeta;
        String reagentId;
        int reagentMeta;
        String outputId;
        int outputMeta;

        public NbtMap toNbt() {
            return NbtMap.builder()
                .putString("inputId", this.inputId)
                .putInt("inputMeta", this.inputMeta)
                .putString("reagentId", this.reagentId)
                .putInt("reagentMeta", this.reagentMeta)
                .putString("outputId", this.outputId)
                .putInt("outputMeta", this.outputMeta)
                .build();
        }
    }

    @Value
    private static class ContainerMixDataEntry {
        String inputId;
        String reagentId;
        String outputId;

        public NbtMap toNbt() {
            return NbtMap.builder()
                .putString("inputId", this.inputId)
                .putString("reagentId", this.reagentId)
                .putString("outputId", this.outputId)
                .build();
        }
    }

    @Value
    private static class MaterialReducerDataEntry {
        int inputId;
        List<RecipeItemDefinitionEntry> itemCounts;

        public NbtMap toNbt() {
            final List<NbtMap> itemCounts = new ObjectArrayList<>();
            for (final RecipeItemDefinitionEntry itemCount : this.itemCounts) {
                itemCounts.add(itemCount.toNbt());
            }
            return NbtMap.builder()
                .putInt("inputId", this.inputId)
                .putList("itemCounts", NbtType.COMPOUND, itemCounts)
                .build();
        }
    }

    @Value
    private static class CraftingDataEntry {
        String id;
        Integer type;
        Object input;
        Object output;
        String[] shape;
        String block;
        UUID uuid;
        Integer netId;
        Integer priority;
        RecipeItemDescriptor base;
        RecipeItemDescriptor addition;
        RecipeItemDescriptor template;
        RecipeItem result;
        Integer width;
        Integer height;
        UnlockingRequirement unlockingRequirements;

        public NbtMap toNbt() {
            final NbtMapBuilder builder = NbtMap.builder();
            if (this.id != null) {
                builder.putString("id", this.id);
            }
            if (this.type != null) {
                builder.putInt("type", this.type);
            }
            if (this.input != null && this.input instanceof List<?>) {
                final List<RecipeItemDescriptor> list = (List<RecipeItemDescriptor>) this.input;
                final List<NbtMap> descriptors = new ObjectArrayList<>();
                for (final RecipeItemDescriptor descriptor : list) {
                    descriptors.add(descriptor.toNbt());
                }
                builder.putList("input", NbtType.COMPOUND, descriptors);
            } else if (this.input != null && this.input instanceof RecipeItem item) {
                builder.putCompound("input", item.toNbt());
            } else if (this.input != null) {
                final Map<Character, RecipeItemDescriptor> map = (Map<Character,
                    RecipeItemDescriptor>) this.input;
                final NbtMapBuilder b = NbtMap.builder();
                for (final Map.Entry<Character, RecipeItemDescriptor> entry : map.entrySet()) {
                    b.putCompound(String.valueOf(entry.getKey()), entry.getValue().toNbt());
                }
                builder.putCompound("input", b.build());
            }
            if (this.output != null && this.output instanceof List<?>) {
                final List<RecipeItem> output = (List<RecipeItem>) this.output;
                final List<NbtMap> outputs = new ObjectArrayList<>();
                for (final RecipeItem item : output) {
                    outputs.add(item.toNbt());
                }
                builder.putList("output", NbtType.COMPOUND, outputs);
            } else if (this.output != null && this.output instanceof RecipeItem recipeItem) {
                builder.putCompound("output", recipeItem.toNbt());
            }
            if (this.shape != null) {
                builder.putList("shape", NbtType.STRING, Arrays.asList(this.shape));
            }
            if (this.block != null) {
                builder.putString("block", this.block);
            }
            if (this.uuid != null) {
                builder.putString("uuid", this.uuid.toString());
            }
            if (this.netId != null) {
                builder.putInt("netId", this.netId);
            }
            if (this.priority != null) {
                builder.putInt("priority", this.priority);
            }
            if (this.base != null) {
                builder.putCompound("base", this.base.toNbt());
            }
            if (this.addition != null) {
                builder.putCompound("addition", this.addition.toNbt());
            }
            if (this.template != null) {
                builder.putCompound("template", this.template.toNbt());
            }
            if (this.result != null) {
                builder.putCompound("result", this.result.toNbt());
            }
            if (this.width != null) {
                builder.putInt("width", this.width);
            }
            if (this.height != null) {
                builder.putInt("height", this.height);
            }
            if (this.unlockingRequirements != null) {
                builder.putCompound("unlockingRequirements", this.unlockingRequirements.toNbt());
            }
            return builder.build();
        }
    }

    @Value
    private static class UnlockingRequirement {
        int context;
        List<RecipeItemDescriptor> items;

        public NbtMap toNbt() {
            final List<NbtMap> items = new ObjectArrayList<>();
            for (final RecipeItemDescriptor item : this.items) {
                items.add(item.toNbt());
            }
            return NbtMap.builder()
                .putInt("context", this.context)
                .putList("items", NbtType.COMPOUND, items)
                .build();
        }
    }

    @Value
    private static class RecipeItemDefinitionEntry {
        String identifier;
        int count;

        public NbtMap toNbt() {
            return NbtMap.builder()
                .putString("identifier", this.identifier)
                .putInt("count", this.count)
                .build();
        }
    }

    @Value
    private static class RecipeItem {
        String id;
        Integer count;
        Integer damage;
        @SerializedName("nbt_b64")
        String nbtBase64;

        public NbtMap toNbt() {
            final NbtMapBuilder builder = NbtMap.builder();
            if (this.id != null) {
                builder.putString("id", this.id);
            }
            if (this.count != null) {
                builder.putInt("count", this.count);
            }
            if (this.damage != null) {
                builder.putInt("damage", this.damage);
            }
            if (this.nbtBase64 != null) {
                builder.putString("nbt_b64", this.nbtBase64);
            }
            return builder.build();
        }
    }

    @Value
    private static class RecipeItemDescriptor {
        String type;
        int count;
        String name;
        String itemId;
        Integer auxValue;
        String fullName;
        String itemTag;
        String tagExpression;
        Integer molangVersion;

        public NbtMap toNbt() {
            final NbtMapBuilder builder = NbtMap.builder();
            if (this.type != null) {
                builder.putString("type", this.type);
            }
            builder.putInt("count", this.count);
            if (this.name != null) {
                builder.putString("name", this.name);
            }
            if (this.itemId != null) {
                builder.putString("itemId", this.itemId);
            }
            if (this.auxValue != null) {
                builder.putInt("auxValue", this.auxValue);
            }
            if (this.fullName != null) {
                builder.putString("fullName", this.fullName);
            }
            if (this.itemTag != null) {
                builder.putString("itemTag", this.itemTag);
            }
            if (this.tagExpression != null) {
                builder.putString("tagExpression", this.tagExpression);
            }
            if (this.molangVersion != null) {
                builder.putInt("molangVersion", this.molangVersion);
            }
            return builder.build();
        }
    }
}