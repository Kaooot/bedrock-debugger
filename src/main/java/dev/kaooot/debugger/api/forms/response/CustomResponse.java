package dev.kaooot.debugger.api.forms.response;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class CustomResponse {

    private final Map<String, Object> responses = new Object2ObjectOpenHashMap<>();

    public void addResponse(String id, Object response) {
        this.responses.put(id, response);
    }

    public String getDropdownResponse(String id) {
        return this.getResponse(id);
    }

    public String getInputResponse(String id) {
        return this.getResponse(id);
    }

    public String getLabelResponse(String id) {
        return this.getResponse(id);
    }

    public Float getSliderResponse(String id) {
        return this.getResponse(id);
    }

    public Float getStepSliderResponse(String id) {
        return this.getResponse(id);
    }

    public Boolean getToggleResponse(String id) {
        return this.getResponse(id);
    }

    private  <T> T getResponse(String id) {
        if (this.responses.containsKey(id)) {
            return (T) this.responses.get(id);
        }
        return null;
    }
}