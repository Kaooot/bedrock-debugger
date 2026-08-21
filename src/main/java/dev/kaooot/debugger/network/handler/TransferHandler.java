package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.TransferPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.MainConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class TransferHandler implements PacketHandler<TransferPacket> {

    @Override
    public PacketSignal handle(TransferPacket packet, BedrockDebuggerProxy proxy) {
        final String serverAddress = packet.getServerAddress();
        final int serverPort = packet.getServerPort();

        proxy.getClient().close("");
        proxy.connect(serverAddress, serverPort);

        final MainConfig config = Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(MainConfig.class);
        packet.setServerAddress("127.0.0.1");
        packet.setServerPort(config.getProxyPort());
        proxy.setTransferring(true);
        proxy.getServer().sendPacket(packet);
        proxy.getPlayers().clear();
        proxy.getActors().clear();
        return PacketSignal.HANDLED;
    }
}