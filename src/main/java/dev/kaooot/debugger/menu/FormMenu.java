package dev.kaooot.debugger.menu;

import dev.kaooot.debugger.BedrockDebuggerProxy;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public interface FormMenu<T extends BedrockDebuggerProxy> {

    void show(T proxy);
}