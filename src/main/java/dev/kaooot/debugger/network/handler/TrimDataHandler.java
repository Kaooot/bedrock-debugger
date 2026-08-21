package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.TrimDataPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;
import dev.kaooot.debugger.util.Util;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class TrimDataHandler implements PacketHandler<TrimDataPacket> {

    @Override
    public PacketSignal handle(TrimDataPacket packet, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setTrimData(
            Util.convertTrimDataToNbt(
                packet.getTrimPatternList(),
                packet.getTrimMaterialList()
            )
        );
        return PacketSignal.UNHANDLED;
    }
}