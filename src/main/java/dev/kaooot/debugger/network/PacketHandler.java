package dev.kaooot.debugger.network;

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public interface PacketHandler<T extends BedrockPacket> {

    PacketSignal handle(T packet, BedrockDebuggerProxy proxy);
}