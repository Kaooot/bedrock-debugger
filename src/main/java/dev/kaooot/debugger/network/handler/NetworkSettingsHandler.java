package dev.kaooot.debugger.network.handler;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import org.cloudburstmc.protocol.bedrock.data.connection.PacketCompressionAlgorithm;
import org.cloudburstmc.protocol.bedrock.packet.NetworkSettingsPacket;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class NetworkSettingsHandler implements PacketHandler<NetworkSettingsPacket> {

    @Override
    public PacketSignal handle(NetworkSettingsPacket packet, BedrockDebuggerProxy proxy) {
        final PacketCompressionAlgorithm algorithm = packet.getCompressionAlgorithm();
        proxy.getServer().sendPacketImmediately(packet);
        proxy.getServer().getEventLoop().execute(() -> {
            proxy.getClient().setCompression(algorithm);
            proxy.getServer().setCompression(algorithm);
            proxy.getLogger().info("Set compression to {}", algorithm);
        });
        return PacketSignal.HANDLED;
    }
}