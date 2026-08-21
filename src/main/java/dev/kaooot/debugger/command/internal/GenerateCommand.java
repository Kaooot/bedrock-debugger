package dev.kaooot.debugger.command.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.command.Command;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public abstract class GenerateCommand extends Command<BedrockDebuggerProxy> {

    protected void saveJsonFile(File file, byte[] data, BedrockDebuggerProxy proxy) {
        this.saveJsonFile(file, data, proxy, JsonObject.class);
    }

    protected void saveJsonFile(File file, byte[] data, BedrockDebuggerProxy proxy,
                                Class<? extends JsonElement> clazz) {
        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(proxy.getGson()
                .toJson(
                    proxy.getGson().fromJson(new String(data), clazz)
                )
                .getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}