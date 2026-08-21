package dev.kaooot.debugger.command.internal;

import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.Command;
import dev.kaooot.debugger.api.command.annotation.Description;
import dev.kaooot.debugger.api.command.annotation.Name;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Name("gamma")
@Description("Enables | Disables Gamma")
public class GammaCommand extends Command<BedrockDebuggerProxy> {

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        final boolean toggled = !proxy.getPlayer().isGammaEnabled();
        proxy.getPlayer().setGammaEnabled(toggled);
        if (toggled) {
            final MobEffectPacket packet = new MobEffectPacket();
            packet.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
            packet.setEvent(MobEffectPacket.Event.ADD);
            packet.setEffectID(16);
            packet.setEffectDurationTicks(Integer.MAX_VALUE);
            proxy.getServer().sendPacket(packet);

            proxy.getPlayer().sendMessage("Enabled Gamma");
        } else {
            final MobEffectPacket packet = new MobEffectPacket();
            packet.setTargetRuntimeID(proxy.getPlayer().getRuntimeId());
            packet.setEvent(MobEffectPacket.Event.REMOVE);
            packet.setEffectID(16);
            proxy.getServer().sendPacket(packet);

            proxy.getPlayer().sendMessage("Disabled Gamma");
        }
    }
}