package dev.kaooot.debugger.network.handler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.payload.creative.CreativeGroupInfoPayload;
import org.cloudburstmc.protocol.bedrock.data.payload.creative.CreativeItemEntryPayload;
import org.cloudburstmc.protocol.bedrock.packet.CreativeContentPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.util.Util;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class CreativeContentHandler implements PacketHandler<CreativeContentPacket> {

    @Override
    public PacketSignal handle(CreativeContentPacket packet, BedrockDebuggerProxy proxy) {
        final List<NbtMap> groups = new ObjectArrayList<>();

        for (final CreativeGroupInfoPayload payload : packet.getGroups()) {
            groups.add(NbtMap.builder()
                .putInt("creative_category", payload.getCreativeCategory().ordinal())
                .putString("name", payload.getName())
                .putCompound("icon",
                    this.buildCreativeItem(payload.getGroupIconItem(), proxy).build())
                .build());
        }

        final List<NbtMap> items = new ObjectArrayList<>();
        for (final CreativeItemEntryPayload payload : packet.getEntries()) {
            final NbtMapBuilder builder = this.buildCreativeItem(payload.getItemInstance(),
                proxy);

            if (payload.getGroupIndex() != -1) {
                builder.putInt("group_index", payload.getGroupIndex());
            }

            items.add(builder.build());
        }
        proxy.getPlayer().setCreativeContents(NbtMap.builder()
            .putList("groups", NbtType.COMPOUND, groups)
            .putList("items", NbtType.COMPOUND, items)
            .build());
        return PacketSignal.UNHANDLED;
    }

    private NbtMapBuilder buildCreativeItem(ItemData content, BedrockDebuggerProxy proxy) {
        final NbtMapBuilder builder = NbtMap.builder();
        final int damage = content.getDamage();

        builder.putString("id", content.getDefinition().getIdentifier());

        if (damage != 0) {
            builder.putInt("damage", damage);
        }

        if (content.getTag() != null) {
            builder.putString("nbt_b64", Util.nbtToBase64(content.getTag()));
        }

        if (content.getBlockDefinition() != null) {
            final int runtimeId = content.getBlockDefinition().getRuntimeId();
            final NbtMap blockState = proxy.getBlockPaletteManager()
                .getBlockStateByRuntimeId(runtimeId,
                    proxy.getPlayer().isBlockNetworkIdsAreHashes());
            if (blockState != null) {
                builder.putString("block_state_b64", Util.nbtToBase64(blockState));
            }
        }
        return builder;
    }
}