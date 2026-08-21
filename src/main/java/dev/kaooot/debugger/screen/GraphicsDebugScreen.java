package dev.kaooot.debugger.screen;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.util.DebugElement;
import dev.kaooot.debugger.util.Util;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import org.cloudburstmc.protocol.bedrock.data.payload.pack.PackInstanceId;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class GraphicsDebugScreen implements DebugScreen {

    @Override
    public void render(BedrockDebuggerProxy proxy) {
        proxy.getServer().sendDebugInfos(
            this.getElement(),
            this.getGraphicNetworkInfos(proxy)
        );
    }

    @Override
    public int getIndex() {
        return 1;
    }

    @Override
    public DebugElement getElement() {
        return DebugElement.CLIENT_NETWORK_INFO;
    }

    private List<String> getGraphicNetworkInfos(BedrockDebuggerProxy proxy) {
        final List<String> graphicNetworkInfos = new ObjectArrayList<>();
        graphicNetworkInfos.add(
            "§lGraphics Network Info§r"
        );
        graphicNetworkInfos.add(
            "Graphics Mode: " + Util.CONVERTER.convert(proxy.getPlayer().getGraphicsMode().name())
        );
        graphicNetworkInfos.add(
            "Are Packs Required?: " + proxy.getPlayer().isResourcePackRequired()
        );
        graphicNetworkInfos.add(
            "Is Force Disable Vibrant Visuals?: " + proxy.getPlayer().isForceDisableVibrantVisuals()
        );
        graphicNetworkInfos.add(
            "Stack Texture Pack Required: " + proxy.getPlayer().isStackTexturePackRequired()
        );
        graphicNetworkInfos.add(
            "Stack Include Editor Packs: " + proxy.getPlayer().isStackIncludeEditorPacks()
        );
        graphicNetworkInfos.add(
            "Stack Base Game Version: " + proxy.getPlayer().getStackBaseGameVersion()
        );
        graphicNetworkInfos.add(" ");
        graphicNetworkInfos.add(
            "Pack Stack (" + proxy.getPlayer().getPackStack().size() + "):"
        );
        if (!proxy.getPlayer().getPackStack().isEmpty()) {
            for (final PackInstanceId entry : proxy.getPlayer().getPackStack()) {
                graphicNetworkInfos.add(entry.getPackID() + " (" + entry.getVersion() + ")");
            }
        }
        return graphicNetworkInfos;
    }
}