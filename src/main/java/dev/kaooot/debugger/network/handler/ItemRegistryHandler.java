package dev.kaooot.debugger.network.handler;

import java.util.List;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.packet.ItemRegistryPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.util.Util;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ItemRegistryHandler implements PacketHandler<ItemRegistryPacket> {

    @Override
    public PacketSignal handle(ItemRegistryPacket packet, BedrockDebuggerProxy proxy) {
        final List<ItemDefinition> sortedItemData = packet.getItemData()
            .stream()
            .sorted((o1, o2) ->
                String.CASE_INSENSITIVE_ORDER.compare(
                    o1.getIdentifier(), o2.getIdentifier()
                )
            ).toList();

        final SimpleDefinitionRegistry.Builder<ItemDefinition> builder =
            proxy.getServer().getItemDefinitions().toBuilder();

        for (final ItemDefinition itemDefinition : packet.getItemData()) {
            builder.add(itemDefinition);
        }

        final SimpleDefinitionRegistry<ItemDefinition> registry = builder.build();

        proxy.getClient().setItemDefinitions(registry);
        proxy.getServer().setItemDefinitions(registry);

        proxy.getPlayer().getItemDefinitions().addAll(sortedItemData);
        proxy.getPlayer().setItemList(
            Util.convertItemDefinitionsToNbt(proxy.getPlayer().getItemDefinitions())
        );

        final NbtMapBuilder itemComponentsBuilder = NbtMap.builder();

        for (final ItemDefinition definition : packet.getItemData()) {
            if (definition.getComponentData().isEmpty()) {
                continue;
            }

            itemComponentsBuilder.putCompound(definition.getIdentifier(), NbtMap.builder()
                .putString("name", definition.getIdentifier())
                .putInt("id", definition.getRuntimeId())
                .putCompound("components", definition.getComponentData()
                    .getCompound("components"))
                .build());
        }

        proxy.getPlayer().setItemComponents(itemComponentsBuilder.build());
        return PacketSignal.UNHANDLED;
    }
}