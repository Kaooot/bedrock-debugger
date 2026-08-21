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
public class Input extends Element {

    private String placeholder;
    private String defaultText;
    private String tooltip;

    @Override
    public JsonObject toJson() {
        final JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("type", "input");
        jsonObject.addProperty("placeholder", this.placeholder);
        jsonObject.addProperty("default", this.defaultText);
        if (this.tooltip != null) {
            jsonObject.addProperty("tooltip", this.tooltip);
        }
        return jsonObject;
    }

    @Override
    public Object getResponse(Object response) {
        this.defaultText = ((JsonPrimitive) super.getResponse(response)).getAsString();
        return this.defaultText;
    }
}