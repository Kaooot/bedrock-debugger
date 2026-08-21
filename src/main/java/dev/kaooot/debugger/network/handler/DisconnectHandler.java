package dev.kaooot.debugger.network.handler;

import java.util.concurrent.TimeUnit;
import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class DisconnectHandler implements PacketHandler<DisconnectPacket> {

    @Override
    public PacketSignal handle(DisconnectPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getLogger().info(
            "Disconnected. Message: {}, reason: {}",
            packet.getMessages().getMessage(),
            packet.getReason().name()
        );
        proxy.getServer().getEventLoop().schedule(() -> {
            proxy.getClient().close(packet.getMessages().getMessage());
            proxy.getServer().close(packet.getMessages().getMessage());
        }, 50, TimeUnit.MILLISECONDS);
        return PacketSignal.UNHANDLED;
    }
}