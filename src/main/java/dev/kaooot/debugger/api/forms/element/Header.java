package dev.kaooot.debugger.api.forms.element;

import com.google.gson.JsonObject;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class Header extends Element {

    public JsonObject toJson() {
        final JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("type", "header");
        return jsonObject;
    }
}