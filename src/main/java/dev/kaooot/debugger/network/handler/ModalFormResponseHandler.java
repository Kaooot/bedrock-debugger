package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.ModalFormResponsePacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ModalFormResponseHandler implements PacketHandler<ModalFormResponsePacket> {

    @Override
    public PacketSignal handle(ModalFormResponsePacket packet, BedrockDebuggerProxy proxy) {
        if (proxy.getPlayer().getFormManager().isValidFormId(packet.getFormID())) {
            proxy.getPlayer().getFormManager().parseFormResponse(packet);
            return PacketSignal.HANDLED;
        }
        return PacketSignal.UNHANDLED;
    }
}