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
public class Slider extends Element {

    private float min;
    private float max;
    private float step;
    private float defaultValue;
    private String tooltip;

    @Override
    public JsonObject toJson() {
        final JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("type", "slider");
        jsonObject.addProperty("min", this.min);
        jsonObject.addProperty("max", this.max);
        if (this.step > 0f) {
            jsonObject.addProperty("step", this.step);
        }
        if (this.defaultValue > this.min) {
            jsonObject.addProperty("default", this.defaultValue);
        }
        if (this.tooltip != null) {
            jsonObject.addProperty("tooltip", this.tooltip);
        }
        return jsonObject;
    }

    @Override
    public Object getResponse(Object response) {
        final double d = ((JsonPrimitive) super.getResponse(response)).getAsDouble();
        this.defaultValue = (float) d;
        return d;
    }
}