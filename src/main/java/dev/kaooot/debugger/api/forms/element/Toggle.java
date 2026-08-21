package dev.kaooot.debugger.api.forms.element;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Toggle extends Element {

    private boolean defaultValue;
    private String tooltip;

    @Override
    public JsonObject toJson() {
        final JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("type", "toggle");
        jsonObject.addProperty("default", this.defaultValue);
        if (this.tooltip != null) {
            jsonObject.addProperty("tooltip", this.tooltip);
        }
        return jsonObject;
    }

    @Override
    public Object getResponse(Object response) {
        this.defaultValue = ((JsonPrimitive) super.getResponse(response)).getAsBoolean();
        return this.defaultValue;
    }
}