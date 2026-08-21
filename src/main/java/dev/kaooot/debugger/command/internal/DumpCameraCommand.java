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
@Name("dumpcamera")
@Description("Outputs valid camera presets nbt in a binary format.")
@Overloads({
    @Parameters(overloads = {
        @Parameter(name = "format", type = CommandParamType.ID, enumData =
        @CommandEnumData(name = "FileFormat", values = {
            @CommandEnumValue(name = "nbt"),
            @CommandEnumValue(name = "json")
        })),
        @Parameter(name = "aim_assist_presets_enabled", type = CommandParamType.ID, enumData =
        @CommandEnumData(name = "AimAssistPresetsEnabled", values = {
            @CommandEnumValue(name = "true"),
            @CommandEnumValue(name = "false")
        }))
    })
})
public class DumpCameraCommand extends DumpPaletteCommand {

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        if (args.length < 2) {
            this.execute0(args, "camera_presets", proxy.getPlayer().getCameraPresets(), proxy);
        } else {
            if (!args[1].equalsIgnoreCase("true") && !args[1].equalsIgnoreCase("false")) {
                proxy.getPlayer().sendMessage("§cInvalid boolean value provided");
                return;
            }
            if (proxy.getPlayer().getCameraAimAssistPresets() == null) {
                proxy.getPlayer().sendMessage("§cAim assist experiment is not enabled");
                return;
            }
            try {
                final boolean b = Boolean.parseBoolean(args[1]);
                this.execute0(args, b ? "camera_aim_assist_presets" : "camera_presets",
                    b ? proxy.getPlayer().getCameraAimAssistPresets() :
                        proxy.getPlayer().getCameraPresets(), proxy);
            } catch (Exception e) {
                proxy.getPlayer().sendMessage("§cInvalid boolean value provided");
            }
        }
    }
}