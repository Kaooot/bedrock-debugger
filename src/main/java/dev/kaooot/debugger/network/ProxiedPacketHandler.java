package dev.kaooot.debugger.network;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.imgui.ImGuiRendererRegistry;
import dev.kaooot.debugger.imgui.renderer.ImGuiMainRenderer;
import dev.kaooot.debugger.screen.ClientNetworkStatsScreen;
import dev.kaooot.debugger.screen.DebugScreenRegistry;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ProxiedPacketHandler implements BedrockPacketHandler {

    private final BedrockDebuggerProxy proxy;
    private final boolean isServer;
    private final ClientNetworkStatsScreen screen;
    private final ImGuiMainRenderer imGuiMainRenderer;

    public ProxiedPacketHandler(BedrockDebuggerProxy proxy, boolean isServer) {
        this.proxy = proxy;
        this.isServer = isServer;
        final DebugScreenRegistry debugScreenRegistry =
            Registries.getRegistry(RegistryKey.DEBUG_SCREEN);
        final ImGuiRendererRegistry imGuiRendererRegistry =
            Registries.getRegistry(RegistryKey.IMGUI_RENDERER);

        this.screen = (ClientNetworkStatsScreen) debugScreenRegistry.getValue(2);
        this.imGuiMainRenderer = (ImGuiMainRenderer) imGuiRendererRegistry
            .getValue(ImGuiMainRenderer.class);
    }

    @Override
    public PacketSignal handlePacket(BedrockPacket packet) {
        this.screen.increasePacketCounter(packet, this.isServer);
        this.imGuiMainRenderer.logPacket(packet);
        final PacketHandler handler;
        if ((handler = Registries.<PacketHandlerRegistry>getRegistry(RegistryKey.PACKET_HANDLER)
            .getValue(packet.getClass())) != null) {
            return handler.handle(packet, this.proxy);
        }
        return BedrockPacketHandler.super.handlePacket(packet);
    }
}