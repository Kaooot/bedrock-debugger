package dev.kaooot.debugger.api.forms;

import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Builder
public class Modal extends BaseForm<Boolean> {

    @Getter
    private final String title;
    private final String content;
    private final String button1Text;
    private final String button2Text;

    @Override
    public FormType getType() {
        return FormType.MODAL;
    }

    @Override
    public Boolean parseResponse(String response) {
        try {
            return Boolean.parseBoolean(response);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public JsonObject toJson() {
        final JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("content", this.content);
        jsonObject.addProperty("button1", this.button1Text);
        jsonObject.addProperty("button2", this.button2Text);
        return jsonObject;
    }
}