package dev.kaooot.debugger.command.internal;

import org.cloudburstmc.protocol.bedrock.data.GameType;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamType;
import org.cloudburstmc.protocol.bedrock.packet.SetPlayerGameTypePacket;
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
@Name("client_gametype")
@Description("Updates the game type on the client-side")
@Overloads({
    @Parameters(overloads = {
        @Parameter(name = "gameType", type = CommandParamType.ID, enumData = @CommandEnumData(
            name = "GameType",
            values = {
                @CommandEnumValue(name = "survival"),
                @CommandEnumValue(name = "adventure"),
                @CommandEnumValue(name = "creative"),
                @CommandEnumValue(name = "default"),
                @CommandEnumValue(name = "spectator"),
            }
        ))
    })
})
public class ClientGameTypeCommand extends Command<BedrockDebuggerProxy> {

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        if (args.length >= 1) {
            final GameType gameType;
            try {
                gameType = GameType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                proxy.getPlayer().sendMessage("§cSpecified invalid game type.");
                return;
            }
            final SetPlayerGameTypePacket packet = new SetPlayerGameTypePacket();
            packet.setPlayerGameType(gameType);
            proxy.getServer().sendPacket(packet);
            proxy.getPlayer().sendMessage("Set client game type to " + args[0].toLowerCase());
        } else {
            proxy.getPlayer().sendMessage("§cSpecified wrong number of arguments.");
        }
    }
}