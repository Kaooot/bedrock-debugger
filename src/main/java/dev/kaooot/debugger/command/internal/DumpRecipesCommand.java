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
import dev.kaooot.debugger.util.RecipeUtil;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Name("dumprecipes")
@Description("Outputs valid recipes nbt in a binary format.")
@Overloads({
    @Parameters(overloads = {
        @Parameter(name = "format", type = CommandParamType.ID, enumData =
        @CommandEnumData(name = "FileFormat", values = {
            @CommandEnumValue(name = "nbt"),
            @CommandEnumValue(name = "json")
        }))
    })
})
public class DumpRecipesCommand extends DumpPaletteCommand {

    private static final RecipeUtil RECIPE_UTIL = new RecipeUtil();

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        this.execute0(
            args,
            "recipes",
            RECIPE_UTIL.parseRecipes(proxy.getPlayer().getCraftingData(), proxy),
            proxy
        );
    }
}