package dev.kaooot.debugger.imgui.renderer;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.imgui.ImGuiAdapter;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public interface ImGuiRenderer {

    void render(BedrockDebuggerProxy proxy, ImGuiAdapter adapter);
}