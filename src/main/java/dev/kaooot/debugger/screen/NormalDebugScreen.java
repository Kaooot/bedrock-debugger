package dev.kaooot.debugger.screen;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.util.DebugElement;
import dev.kaooot.debugger.util.Util;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class NormalDebugScreen implements DebugScreen {

    @Override
    public void render(BedrockDebuggerProxy proxy) {
        proxy.getServer().sendDebugInfos(
            this.getElement(),
            this.getInfos(proxy)
        );
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public DebugElement getElement() {
        return DebugElement.CLIENT_NETWORK_INFO;
    }

    public List<String> getInfos(BedrockDebuggerProxy proxy) {
        final int chunkX = proxy.getPlayer().getChunkX();
        final int chunkZ = proxy.getPlayer().getChunkZ();
        final List<String> infos = new ObjectArrayList<>();
        infos.add(
            "Minecraft " + proxy.getDebugScreenInfo().getMinecraftVersion()
        );
        infos.add(
            "Mode: " + Util.CONVERTER.convert(proxy.getPlayer().getGameType().name()) + " (online)"
        );
        infos.add(
            "XYZ: " +
                Util.round(proxy.getPlayer().getPosition().getX(), 3) + " / " +
                Util.round(proxy.getPlayer().getPosition().getY(), 5) + " / " +
                Util.round(proxy.getPlayer().getPosition().getZ(), 3)
        );
        infos.add("Rotation XYZ: " +
            Util.round(proxy.getPlayer().getRotation().getX(), 3) + " / " +
            Util.round(proxy.getPlayer().getRotation().getY(), 3) + " / " +
            Util.round(proxy.getPlayer().getRotation().getZ(), 3)
        );
        infos.add("Direction: " + proxy.getPlayer().getDirection());
        infos.add(
            "Sent (zip): " +
                Util.round((float) proxy.getClient().getSentCountAvg() / 1024f) + " KB/s"
        );
        infos.add(
            "Received (zip): " +
                Util.round((float) proxy.getClient().getReceivedCountAvg() / 1024f) + " KB/s"
        );
        infos.add(
            "Block: " + proxy.getPlayer().getBlockBelow().getX() +
                " " + proxy.getPlayer().getBlockBelow().getY() +
                " " + proxy.getPlayer().getBlockBelow().getZ()
        );
        infos.add(
            "Chunk: " +
                chunkX +
                " " + (proxy.getPlayer().getBlockBelow().getY() >> 4) +
                " " + chunkZ
        );
        infos.add(
            !proxy.getPlayer().getCurrentStructureFeature().isEmpty() &&
                !proxy.getPlayer().getCurrentStructureFeature().equalsIgnoreCase("null") ?
                "Structure: " + proxy.getPlayer().getCurrentStructureFeature() : ""
        );
        infos.add(
            !proxy.getPlayer().getExperiments().isEmpty() &&
                Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG)
                    .get(SettingsConfig.class).isRenderExperimentInfo() ? "Experiments:\n- " +
                String.join("\n- ", proxy.getPlayer().getExperiments()) : ""
        );
        return infos;
    }
}