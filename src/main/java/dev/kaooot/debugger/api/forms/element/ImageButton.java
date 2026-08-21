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
public class ImageButton extends Button {

    private String image;

    @Override
    public JsonObject toJson() {
        final JsonObject jsonObject = super.toJson();
        final JsonObject image = new JsonObject();
        image.addProperty("type", this.image.startsWith("http") ? "url" : "path");
        image.addProperty("data", this.image);
        jsonObject.add("image", image);
        return jsonObject;
    }
}