package dev.kaooot.debugger.network.handler;

import java.lang.reflect.Field;
import java.util.List;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.PrimitiveShapeDataPayload;
import org.cloudburstmc.protocol.bedrock.packet.PrimitiveShapesPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.shape.DebugShapeRenderer;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class PrimitiveShapesHandler implements PacketHandler<PrimitiveShapesPacket> {

    @Override
    public PacketSignal handle(PrimitiveShapesPacket packet, BedrockDebuggerProxy proxy) {
        final List<Long> internalNetworkIds = this.getInternalNetworkIds(proxy);
        for (final PrimitiveShapeDataPayload shape : packet.getShapes()) {
            if (shape.getScale() != null && shape.getScale() > 0f) {
                internalNetworkIds.add(shape.getNetworkId());
            } else {
                internalNetworkIds.remove(shape.getNetworkId());
            }
        }
        return PacketSignal.UNHANDLED;
    }

    private List<Long> getInternalNetworkIds(BedrockDebuggerProxy proxy) {
        final DebugShapeRenderer renderer = proxy.getDebugShapeRenderer();

        try {
            final Field field = renderer.getClass().getDeclaredField("usedNetworkIds");
            field.setAccessible(true);
            return (List<Long>) field.get(renderer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to get internal network ids", e);
        }
    }
}