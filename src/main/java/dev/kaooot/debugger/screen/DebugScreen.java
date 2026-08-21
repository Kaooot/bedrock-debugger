package dev.kaooot.debugger.screen;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.util.DebugElement;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public interface DebugScreen {

    void render(BedrockDebuggerProxy proxy);

    int getIndex();

    DebugElement getElement();
}