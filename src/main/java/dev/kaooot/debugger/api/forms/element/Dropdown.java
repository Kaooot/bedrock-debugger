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
public class Dropdown extends Element {

    private final List<String> options = new ObjectArrayList<>();
    private int defaultOption;
    @Setter
    @Getter
    private String tooltip;

    @Override
    public JsonObject toJson() {
        final JsonObject jsonObject = super.toJson();
        final JsonArray options = new JsonArray();
        for (final String option : this.options) {
            options.add(option);
        }
        jsonObject.addProperty("type", "dropdown");
        jsonObject.add("options", options);
        jsonObject.addProperty("default", this.defaultOption);
        if (this.tooltip != null) {
            jsonObject.addProperty("tooltip", this.tooltip);
        }
        return jsonObject;
    }

    @Override
    public String getResponse(Object response) {
        final long l = ((JsonPrimitive) super.getResponse(response)).getAsLong();
        this.defaultOption = (int) l;
        return this.options.get(this.defaultOption);
    }

    public void addOption(String option, boolean def) {
        if (def) {
            this.defaultOption = this.options.size();
        }
        this.options.add(option);
    }

    public void addOption(String option) {
        this.addOption(option, false);
    }

    public int getDefault() {
        return this.defaultOption;
    }
}