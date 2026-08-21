package dev.kaooot.debugger.api.forms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public abstract class BaseForm<R> {

    public abstract FormType getType();

    public abstract R parseResponse(String response);

    public abstract String getTitle();

    public JsonObject toJson() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", this.getType().name().toLowerCase());
        jsonObject.addProperty("title", this.getTitle());
        jsonObject.add("content", new JsonArray());
        return jsonObject;
    }
}