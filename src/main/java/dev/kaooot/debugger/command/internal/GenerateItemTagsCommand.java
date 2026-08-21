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
@Name("generateitemtags")
@Description("Outputs valid item tags in a json format.")
public class GenerateItemTagsCommand extends GenerateCommand {

    private boolean flag;

    @Override
    public void execute(String command, String[] args, BedrockDebuggerProxy proxy) {
        if (this.flag) {
            proxy.getPlayer().sendMessage("§cFailure! :( Error: Flag is set.");
            return;
        }
        this.flag = true;
        final String fileName = "item_tags";
        final File file = new File(proxy.getDataFolder() + "/logs/" + fileName + ".json");
        final DebugServerHelper helper = new DebugServerHelper(proxy);
        helper.startDebugServer().whenComplete((result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                proxy.getPlayer().sendMessage("§cFailure :( Error: " + throwable.getMessage());
                return;
            }

            proxy.getDebugHttpServer().addListener(DebugHttpServer.ListenerType.ITEM_TAGS,
                itemTags -> {
                    this.saveJsonFile(file, itemTags, proxy);

                    helper.stopDebugServer();

                    proxy.getPlayer().sendMessage("Success! :) File output to: " +
                        file.getAbsolutePath());

                    this.flag = false;
                });
        });
    }
}