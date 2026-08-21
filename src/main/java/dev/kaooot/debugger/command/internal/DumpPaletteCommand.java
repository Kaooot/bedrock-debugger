package dev.kaooot.debugger.command.internal;

import java.io.File;
import org.cloudburstmc.nbt.NbtMap;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.Command;
import dev.kaooot.debugger.util.Util;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public abstract class DumpPaletteCommand extends Command<BedrockDebuggerProxy> {

    protected void execute0(String[] args, String fileName, NbtMap nbtMap,
                            BedrockDebuggerProxy proxy) {
        File file = new File(proxy.getDataLogsFolder(), fileName + ".nbt");
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("nbt")) {
                Util.dumpPaletteNbt(nbtMap, file);
                this.sendSuccessMessage(proxy, file);
            } else if (args[0].equalsIgnoreCase("json")) {
                file = new File(proxy.getDataLogsFolder(), fileName + ".json");
                Util.dumpPaletteJson(nbtMap, proxy, file);
                this.sendSuccessMessage(proxy, file);
            }
        } else {
            Util.dumpPaletteNbt(nbtMap, file);
            this.sendSuccessMessage(proxy, file);
        }
    }

    protected void sendSuccessMessage(BedrockDebuggerProxy proxy, File file) {
        proxy.getPlayer().sendMessage("Success! :) File output to: " + file.getAbsolutePath());
    }
}