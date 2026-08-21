package dev.kaooot.debugger.network.handler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.data.VoxelShapes;
import org.cloudburstmc.protocol.bedrock.packet.VoxelShapesPacket;
import org.cloudburstmc.protocol.common.PacketSignal;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.PacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class VoxelShapesHandler implements PacketHandler<VoxelShapesPacket> {

    @Override
    public PacketSignal handle(VoxelShapesPacket packet, BedrockDebuggerProxy proxy) {
        final List<NbtMap> shapes = new ObjectArrayList<>();
        for (final VoxelShapes.SerializableVoxelShape shape : packet.getShapes()) {
            final NbtMapBuilder cellsBuilder = NbtMap.builder();
            cellsBuilder.putInt("xSize", shape.getCells().getXSize());
            cellsBuilder.putInt("ySize", shape.getCells().getYSize());
            cellsBuilder.putInt("zSize", shape.getCells().getZSize());
            cellsBuilder.putList("storage", NbtType.INT, shape.getCells().getStorage());

            shapes.add(
                NbtMap.builder()
                    .putCompound("cells", cellsBuilder.build())
                    .putList("xCoordinates", NbtType.FLOAT, shape.getXCoordinates())
                    .putList("yCoordinates", NbtType.FLOAT, shape.getYCoordinates())
                    .putList("zCoordinates", NbtType.FLOAT, shape.getZCoordinates())
                    .build()
            );
        }

        final NbtMapBuilder nameMapBuilder = NbtMap.builder();
        for (final String s : packet.getNameMap().keySet()) {
            nameMapBuilder.putInt(s, packet.getNameMap().get(s).getValue());
        }

        proxy.getPlayer().setVoxelShapes(
            NbtMap.builder()
                .putList("shapes", NbtType.COMPOUND, shapes)
                .putCompound("nameMap", nameMapBuilder.build())
                .putInt("customShapeCount", packet.getCustomShapeCount())
                .build()
        );
        return PacketSignal.UNHANDLED;
    }
}