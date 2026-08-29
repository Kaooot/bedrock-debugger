package dev.kaooot.debugger.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.cloudburstmc.protocol.bedrock.BedrockClientSession;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
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
public class ProxiedClientSession extends BedrockClientSession {

    private final BedrockDebuggerProxy proxy;

    public ProxiedClientSession(BedrockDebuggerProxy proxy, BedrockPeer peer, int subClientId) {
        super(peer, subClientId);
        this.proxy = proxy;
    }

    @Override
    protected void onPacket(BedrockPacketWrapper wrapper) {
        final BedrockPacket packet = wrapper.getPacket();

        this.proxy.getPacketLog().capture(wrapper, PacketRecipient.CLIENT);

        if (this.packetHandler == null) {
            this.proxy.getLogger().warn("Received packet without handler for {}:{}",
                this.peer.getSocketAddress(), this.subClientId);
        } else if (this.packetHandler.handlePacket(packet).equals(PacketSignal.UNHANDLED) &&
            this.proxy.getServer().isConnected()) {
            final ByteBuf buffer = wrapper.getPacketBuffer()
                .retainedSlice()
                .skipBytes(wrapper.getHeaderLength());

            if (wrapper.getPacket().getPacketType().equals(BedrockPacketType.UNKNOWN)) {
                this.proxy.getLogger().warn("Server sent unknown packet with id: {} ({})",
                    wrapper.getPacketId(), ByteBufUtil.hexDump(wrapper.getPacketBuffer()));
            }

            try {
                final BedrockPacket pk = this.proxy.getServer().getCodec().tryDecode(
                    this.proxy.getServer().getCodecHelper(),
                    buffer,
                    wrapper.getPacketId(),
                    PacketRecipient.CLIENT
                );
                this.proxy.getServer().sendPacket(pk);
            } finally {
                buffer.release();
            }
        }
    }
}