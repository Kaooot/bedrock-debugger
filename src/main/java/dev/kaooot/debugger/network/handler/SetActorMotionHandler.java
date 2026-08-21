package dev.kaooot.debugger.network.handler;

import org.cloudburstmc.protocol.bedrock.packet.SetActorMotionPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.actor.Actor;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class SetActorMotionHandler implements PacketHandler<SetActorMotionPacket> {

    @Override
    public PacketSignal handle(SetActorMotionPacket packet, BedrockDebuggerProxy proxy) {
        for (final Actor actor : proxy.getActors()) {
            if (actor.getRuntimeId() == packet.getTargetRuntimeID()) {
                actor.setPosition(actor.getPosition().clone().add(packet.getMotion()));
                break;
            }
        }
        return PacketSignal.UNHANDLED;
    }
}