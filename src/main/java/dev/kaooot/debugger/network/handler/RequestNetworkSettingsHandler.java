package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.data.PlayStatus;
import org.cloudburstmc.protocol.bedrock.packet.PlayStatusPacket;
import org.cloudburstmc.protocol.bedrock.packet.RequestNetworkSettingsPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.network.NetworkConstants;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class RequestNetworkSettingsHandler implements PacketHandler<RequestNetworkSettingsPacket> {

    @Override
    public PacketSignal handle(RequestNetworkSettingsPacket packet, BedrockDebuggerProxy proxy) {
        int clientNetworkVersion = packet.getClientNetworkVersion();
        final int expectedClientNetworkVersion = NetworkConstants.CODEC.getProtocolVersion();

        final boolean overrideClientNetworkVersion = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class).isOverrideClientNetworkVersion();

        if (overrideClientNetworkVersion) {
            clientNetworkVersion = expectedClientNetworkVersion;
        }

        if (expectedClientNetworkVersion != clientNetworkVersion) {
            final PlayStatusPacket playStatusPacket = new PlayStatusPacket();

            if (clientNetworkVersion > expectedClientNetworkVersion) {
                playStatusPacket.setStatus(PlayStatus.LOGIN_FAILED_SERVER_OLD);
            } else {
                playStatusPacket.setStatus(PlayStatus.LOGIN_FAILED_CLIENT_OLD);
            }

            proxy.getServer().sendPacketImmediately(playStatusPacket);
            return PacketSignal.HANDLED;
        }
        if (overrideClientNetworkVersion) {
            packet.setClientNetworkVersion(expectedClientNetworkVersion);
            proxy.getClient().sendPacket(packet);
        }
        proxy.getClient().setEncodingSettings(NetworkConstants.ENCODING_SETTINGS);
        proxy.getServer().setEncodingSettings(NetworkConstants.ENCODING_SETTINGS);
        return overrideClientNetworkVersion ? PacketSignal.HANDLED : PacketSignal.UNHANDLED;
    }
}