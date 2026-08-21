package dev.kaooot.debugger.command.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamType;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.annotation.CommandEnumData;
import dev.kaooot.debugger.api.command.annotation.CommandEnumValue;
import dev.kaooot.debugger.api.command.annotation.Description;
import dev.kaooot.debugger.api.command.annotation.Name;
import dev.kaooot.debugger.api.command.annotation.Overloads;
import dev.kaooot.debugger.api.command.annotation.Parameter;
import dev.kaooot.debugger.api.command.annotation.Parameters;
import dev.kaooot.debugger.util.Util;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Name("dumpblockpalette")
@Description("Outputs valid block nbt in a binary format.")
@Overloads({
    @Parameters(overloads = {
        @Parameter(name = "format", type = CommandParamType.ID, enumData =
        @CommandEnumData(name = "FileFormat", values = {
            @CommandEnumValue(name = "nbt"),
            @CommandEnumValue(name = "json")
        }))
    })
})
public class DumpBlockPaletteCommand extends DumpPaletteCommand {

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        File file = new File(proxy.getDataLogsFolder(), "block_palette.nbt");
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("nbt")) {
                Util.dumpPaletteNbt(proxy.getBlockPaletteManager().getBlockPalette(), file);
                this.sendSuccessMessage(proxy, file);
            } else if (args[0].equalsIgnoreCase("json")) {
                file = new File(proxy.getDataLogsFolder(), "block_palette.json");
                this.dump(file, proxy.getGson().toJson(proxy.getBlockPaletteManager()
                    .getBlockPaletteJson()));
                this.sendSuccessMessage(proxy, file);
            }
        } else {
            Util.dumpPaletteNbt(proxy.getBlockPaletteManager().getBlockPalette(), file);
            this.sendSuccessMessage(proxy, file);
        }
    }

    private void dump(File file, String data) {
        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(data.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}