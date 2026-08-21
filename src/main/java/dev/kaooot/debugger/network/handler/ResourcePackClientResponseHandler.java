package dev.kaooot.debugger.network.handler;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.UUID;
import org.cloudburstmc.protocol.bedrock.data.ResourcePackResponse;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePackDataInfoPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.pack.ServerPack;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ResourcePackClientResponseHandler
    implements PacketHandler<ResourcePackClientResponsePacket> {

    private final int maxChunkSize = 8192;

    @Override
    public PacketSignal handle(ResourcePackClientResponsePacket packet,
                               BedrockDebuggerProxy proxy) {
        if (packet.getResponse().equals(ResourcePackResponse.DOWNLOADING)) {
            final Set<String> removable = new ObjectOpenHashSet<>();
            for (final String downloadingPack : packet.getDownloadingPacks()) {
                final ServerPack pack;
                if ((pack = proxy.getPackManager().getPackIdCache()
                    .get(UUID.fromString(downloadingPack.contains("_") ?
                        downloadingPack.split("_")[0] : downloadingPack))) != null) {
                    final ResourcePackDataInfoPacket resourcePackDataInfoPacket =
                        new ResourcePackDataInfoPacket();
                    resourcePackDataInfoPacket.setPackId(pack.getId());
                    resourcePackDataInfoPacket.setPackVersion(pack.getVersion());
                    resourcePackDataInfoPacket.setChunkSize(this.maxChunkSize);
                    resourcePackDataInfoPacket.setNumberOfChunks(
                        pack.getSize() / this.maxChunkSize
                    );
                    resourcePackDataInfoPacket.setFileSize(pack.getSize());
                    resourcePackDataInfoPacket.setFileHash(pack.getHash());
                    resourcePackDataInfoPacket.setPackType(pack.getType());

                    proxy.getServer().sendPacket(resourcePackDataInfoPacket);
                    removable.add(downloadingPack);
                }
            }
            for (final String downloadingPack : removable) {
                packet.getDownloadingPacks().remove(downloadingPack);
            }
        }
        proxy.getClient().sendPacket(packet);
        return PacketSignal.HANDLED;
    }
}