package dev.kaooot.debugger.command.internal;

import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.Command;
import dev.kaooot.debugger.api.command.annotation.Description;
import dev.kaooot.debugger.api.command.annotation.Name;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Name("pause")
@Description("Enables | Disables Pause")
public class PauseCommand extends Command<BedrockDebuggerProxy> {

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        proxy.getPlayer().setPaused(!proxy.getPlayer().isPaused());

        final LevelEventPacket packet = new LevelEventPacket();
        packet.setType(LevelEvent.GLOBAL_PAUSE);
        packet.setPosition(proxy.getPlayer().getPosition());
        packet.setData(proxy.getPlayer().isPaused() ? (byte) 1 : (byte) 0);
        proxy.getServer().sendPacket(packet);

        proxy.getPlayer().sendMessage(
            (proxy.getPlayer().isPaused() ? "Enabled" : "Disabled") + " Pause"
        );
    }
}