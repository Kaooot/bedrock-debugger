package dev.kaooot.debugger.api.forms.element;

import com.google.gson.JsonObject;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class Label extends Element {

    @Override
    public JsonObject toJson() {
        final JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("type", "label");
        return jsonObject;
    }
}