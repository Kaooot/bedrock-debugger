package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.BiomeDefinitionListPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.util.BiomeUtil;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class BiomeDefinitionListHandler implements PacketHandler<BiomeDefinitionListPacket> {

    @Override
    public PacketSignal handle(BiomeDefinitionListPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setBiomeData(BiomeUtil.parseBiomeDefinitionList(packet));
        return PacketSignal.UNHANDLED;
    }
}