package dev.kaooot.debugger.command.internal;

import org.cloudburstmc.protocol.bedrock.data.command.CommandParamType;
import org.cloudburstmc.protocol.bedrock.packet.SetTimePacket;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.Command;
import dev.kaooot.debugger.api.command.annotation.CommandEnumData;
import dev.kaooot.debugger.api.command.annotation.CommandEnumValue;
import dev.kaooot.debugger.api.command.annotation.Description;
import dev.kaooot.debugger.api.command.annotation.Name;
import dev.kaooot.debugger.api.command.annotation.Overloads;
import dev.kaooot.debugger.api.command.annotation.Parameter;
import dev.kaooot.debugger.api.command.annotation.Parameters;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Name("lock_day_time")
@Description("Locks and unlocks the day-night cycle.")
@Overloads({
    @Parameters(overloads = {
        @Parameter(name = "lock", type = CommandParamType.ID, enumData =
        @CommandEnumData(name = "Boolean", values = {
            @CommandEnumValue(name = "false"),
            @CommandEnumValue(name = "true")
        }))
    })
})
public class LockDayTimeCommand extends Command<BedrockDebuggerProxy> {

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("true")) {
                final SetTimePacket packet = new SetTimePacket();
                packet.setTime(6000);

                proxy.getServer().sendPacket(packet);
                proxy.getPlayer().setAlwaysDay(true);
                proxy.getPlayer().sendMessage("Enabled day time lock");
            } else if (args[0].equalsIgnoreCase("false")) {
                final SetTimePacket packet = new SetTimePacket();
                packet.setTime(proxy.getPlayer().getLevelTime());

                proxy.getServer().sendPacket(packet);
                proxy.getPlayer().setAlwaysDay(false);
                proxy.getPlayer().sendMessage("Disabled day time lock");
            }
        }
    }
}