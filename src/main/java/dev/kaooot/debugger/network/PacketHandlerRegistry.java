package dev.kaooot.debugger.network;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import dev.kaooot.debugger.core.registry.Registry;
import dev.kaooot.debugger.core.registry.RegistryKey;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class PacketHandlerRegistry
    extends Registry<Class<? extends BedrockPacket>, PacketHandler<? extends BedrockPacket>> {

    @Override
    public RegistryKey getKey() {
        return RegistryKey.PACKET_HANDLER;
    }

    @Override
    protected void register(PacketHandler<?> type) {
        final Class<? extends BedrockPacket> packetClass =
            (Class<? extends BedrockPacket>) ((ParameterizedType) type.getClass()
                .getGenericInterfaces()[0]).getActualTypeArguments()[0];

        try {
            this.registry.put(packetClass, (PacketHandler<? extends BedrockPacket>)
                type.getClass().getConstructors()[0].newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean validateType(Class<?> clazz) {
        if (clazz.getGenericInterfaces().length < 1) {
            return false;
        }
        return ((ParameterizedType) clazz.getGenericInterfaces()[0]).getRawType()
            .equals(PacketHandler.class);
    }
}