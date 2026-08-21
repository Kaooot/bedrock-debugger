package dev.kaooot.debugger.api.forms.element;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class Element {

    private String id;
    private String text = "";

    public JsonObject toJson() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", this.text);
        return jsonObject;
    }

    public Object getResponse(Object response) {
        return response;
    }
}