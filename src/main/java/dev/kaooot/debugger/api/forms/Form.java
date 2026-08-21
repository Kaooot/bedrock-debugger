package dev.kaooot.debugger.api.forms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import dev.kaooot.debugger.api.forms.element.Button;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Builder
public class Form extends BaseForm<String> {

    @Getter
    private String title;
    private String content;
    private final Map<String, Button> buttons = new LinkedHashMap<>();

    @Override
    public FormType getType() {
        return FormType.FORM;
    }

    @Override
    public String parseResponse(String json) {
        try {
            final int id = Integer.parseInt(json.trim());
            final List<String> buttonIds = new ObjectArrayList<>();
            for (final Map.Entry<String, Button> entry : this.buttons.entrySet()) {
                buttonIds.add(entry.getKey());
            }
            return id > this.buttons.size() ? null : buttonIds.get(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public JsonObject toJson() {
        final JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("content", this.content);
        final JsonArray jsonArray = new JsonArray();
        for (final Button button : this.buttons.values()) {
            jsonArray.add(button.toJson());
        }
        jsonObject.add("buttons", jsonArray);
        return jsonObject;
    }

    public <T extends Button> Form add(T... buttons) {
        for (final T button : buttons) {
            this.buttons.put(button.getId(), button);
        }
        return this;
    }

    public <T extends Button> T get(String id) {
        if (!this.buttons.containsKey(id)) {
            return null;
        }
        return (T) this.buttons.get(id);
    }
}