package dev.kaooot.debugger.imgui;

import dev.kaooot.debugger.core.registry.Registry;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.imgui.renderer.ImGuiRenderer;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ImGuiRendererRegistry extends Registry<Class<? extends ImGuiRenderer>, ImGuiRenderer> {

    @Override
    public RegistryKey getKey() {
        return RegistryKey.IMGUI_RENDERER;
    }

    @Override
    protected void register(ImGuiRenderer type) {
        this.registry.put(type.getClass(), type);
    }

    @Override
    protected boolean validateType(Class<?> clazz) {
        return clazz.getInterfaces().length >= 1 &&
            clazz.getInterfaces()[0].equals(ImGuiRenderer.class);
    }
}