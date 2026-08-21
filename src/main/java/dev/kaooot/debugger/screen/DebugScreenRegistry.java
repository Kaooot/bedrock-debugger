package dev.kaooot.debugger.screen;

import dev.kaooot.debugger.core.registry.Registry;
import dev.kaooot.debugger.core.registry.RegistryKey;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class DebugScreenRegistry extends Registry<Integer, DebugScreen> {

    @Override
    public RegistryKey getKey() {
        return RegistryKey.DEBUG_SCREEN;
    }

    @Override
    protected void register(DebugScreen screen) {
        this.registry.put(screen.getIndex(), screen);
    }

    @Override
    protected boolean validateType(Class<?> clazz) {
        if (clazz.getGenericInterfaces().length < 1) {
            return false;
        }
        return clazz.getGenericInterfaces()[0].equals(DebugScreen.class);
    }
}