package dev.kaooot.debugger.api.forms;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import dev.kaooot.debugger.api.forms.element.Element;
import dev.kaooot.debugger.api.forms.response.CustomResponse;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Builder
public class CustomForm extends BaseForm<CustomResponse> {

    @Getter
    private String title;
    private String icon;
    private final List<Element> elements = new ObjectArrayList<>();

    @Override
    public FormType getType() {
        return FormType.CUSTOM_FORM;
    }

    @Override
    public CustomResponse parseResponse(String response) {
        final CustomResponse customResponse = new CustomResponse();
        final JsonArray jsonArray = new Gson().fromJson(response, JsonArray.class);
        for (int i = 0; i < jsonArray.size(); i++) {
            final Object responseObj = jsonArray.get(i);
            customResponse.addResponse(this.elements.get(i).getId(),
                this.elements.get(i).getResponse(responseObj));
        }
        return customResponse;
    }

    @Override
    public JsonObject toJson() {
        final JsonObject jsonObject = super.toJson();
        final JsonArray jsonArray = new JsonArray();
        for (final Element element : this.elements) {
            jsonArray.add(element.toJson());
        }
        jsonObject.add("content", jsonArray);
        if (this.icon != null) {
            final JsonObject icon = new JsonObject();
            icon.addProperty("type", this.icon.startsWith("http") ? "url" : "path");
            icon.addProperty("data", this.icon);
            jsonObject.add("icon", icon);
        }
        return jsonObject;
    }

    public <T extends Element> CustomForm add(T... elements) {
        this.elements.addAll(Arrays.asList(elements));
        return this;
    }

    public <T extends Element> T get(String id) {
        for (final Element element : this.elements) {
            if (element.getId().equalsIgnoreCase(id)) {
                return (T) element;
            }
        }
        return null;
    }
}