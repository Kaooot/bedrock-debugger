package dev.kaooot.debugger.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.data.PacketRecipient;
import org.cloudburstmc.protocol.bedrock.netty.BedrockPacketWrapper;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ProxiedServerSession extends BedrockServerSession {

    private final BedrockDebuggerProxy proxy;

    public ProxiedServerSession(BedrockDebuggerProxy proxy, BedrockPeer peer, int subClientId) {
        super(peer, subClientId);
        this.proxy = proxy;
    }

    @Override
    protected void onPacket(BedrockPacketWrapper wrapper) {
        final BedrockPacket packet = wrapper.getPacket();

        this.proxy.getPacketLog().capture(wrapper, PacketRecipient.SERVER);

        if (this.packetHandler == null) {
            this.proxy.getLogger().warn("Received packet without handler for {}:{}",
                this.peer.getSocketAddress(), this.subClientId);
        } else if (this.packetHandler.handlePacket(packet).equals(PacketSignal.UNHANDLED)) {
            final ByteBuf buffer = wrapper.getPacketBuffer()
                .retainedSlice()
                .skipBytes(wrapper.getHeaderLength());

            if (wrapper.getPacket().getPacketType().equals(BedrockPacketType.UNKNOWN)) {
                this.proxy.getLogger().warn("Client sent unknown packet with id: {} ({})",
                    wrapper.getPacketId(), ByteBufUtil.hexDump(wrapper.getPacketBuffer()));
            }

            try {
                final BedrockPacket pk = this.proxy.getClient().getCodec().tryDecode(
                    this.proxy.getClient().getCodecHelper(),
                    buffer,
                    wrapper.getPacketId(),
                    PacketRecipient.SERVER
                );
                this.proxy.getClient().sendPacket(pk);
            } finally {
                buffer.release();
            }
        }
    }
}