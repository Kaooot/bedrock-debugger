package dev.kaooot.debugger.command.internal;

import java.io.File;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.annotation.Description;
import dev.kaooot.debugger.api.command.annotation.Name;
import dev.kaooot.debugger.util.DebugHttpServer;
import dev.kaooot.debugger.util.DebugServerHelper;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Name("generateblockpalette")
@Description("Generates the block palette.")
public class GenerateBlockPaletteCommand extends GenerateCommand {

    private boolean flag;

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        if (this.flag) {
            proxy.getPlayer().sendMessage("§cFailure! :( Error: Flag is set.");
            return;
        }
        this.flag = true;
        final DebugServerHelper helper = new DebugServerHelper(proxy);
        helper.startDebugServer().whenComplete((result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                proxy.getPlayer().sendMessage("§cFailure :( Error: " + throwable.getMessage());
                return;
            }

            proxy.getDebugHttpServer().addListener(DebugHttpServer.ListenerType.BLOCKS, blocks -> {
                try {
                    final File outputFile = proxy.getBlockPaletteGenerator().generate(blocks);
                    proxy.getPlayer().sendMessage(
                        "Success! :) File output to: " + outputFile.getAbsolutePath()
                    );
                } catch (Exception e) {
                    proxy.getPlayer().sendMessage("§cFailure :( Error: " + e.getMessage());
                }
                helper.stopDebugServer();
                this.flag = false;
            });
        });
    }
}