package dev.kaooot.debugger.api.forms.element;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class StepSlider extends Element {

    private final List<String> steps = new ObjectArrayList<>();
    private int defaultStep;
    @Setter
    @Getter
    private String tooltip;

    @Override
    public JsonObject toJson() {
        final JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("type", "step_slider");
        final JsonArray steps = new JsonArray();
        for (final String step : this.steps) {
            steps.add(step);
        }
        jsonObject.add("steps", steps);
        jsonObject.addProperty("default", this.defaultStep);
        if (this.tooltip != null) {
            jsonObject.addProperty("tooltip", this.tooltip);
        }
        return jsonObject;
    }

    @Override
    public Object getResponse(Object response) {
        final String s = ((JsonPrimitive) super.getResponse(response)).getAsString();
        this.defaultStep = this.steps.indexOf(s);
        return s;
    }

    public void addStep(String step, boolean def) {
        if (def) {
            this.defaultStep = this.steps.size();
        }
        this.steps.add(step);
    }

    public void addStep(String step) {
        this.addStep(step, false);
    }
}