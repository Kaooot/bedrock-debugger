package dev.kaooot.debugger.command.internal;

import org.cloudburstmc.protocol.bedrock.data.command.CommandParamType;
import dev.kaooot.debugger.BedrockDebuggerProxy;
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
@Name("dumpcreativeitems")
@Description("Outputs valid creative item nbt in a binary format.")
@Overloads({
    @Parameters(overloads = {
        @Parameter(name = "format", type = CommandParamType.ID, enumData =
        @CommandEnumData(name = "FileFormat", values = {
            @CommandEnumValue(name = "nbt"),
            @CommandEnumValue(name = "json")
        }))
    })
})
public class DumpCreativeItemsCommand extends DumpPaletteCommand {

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        if (proxy.getPlayer().getCreativeContents() == null) {
            proxy.getPlayer().sendMessage("Failure :(");
            return;
        }
        this.execute0(args, "creative_items", proxy.getPlayer().getCreativeContents(), proxy);
    }
}