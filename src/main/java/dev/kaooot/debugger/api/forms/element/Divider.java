package dev.kaooot.debugger.api.forms.element;

import com.google.gson.JsonObject;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class Divider extends Element {

    public JsonObject toJson() {
        final JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("type", "divider");
        return jsonObject;
    }
}