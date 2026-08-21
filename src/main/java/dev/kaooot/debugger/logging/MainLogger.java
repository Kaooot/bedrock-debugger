package dev.kaooot.debugger.logging;

import lombok.extern.log4j.Log4j2;
import dev.kaooot.debugger.api.logging.Logger;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Log4j2
public class MainLogger implements Logger {

    @Override
    public void info(String message, Object... args) {
        log.info(message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        log.warn(message, args);
    }

    @Override
    public void error(String message, Object... args) {
        log.error(message, args);
    }

    @Override
    public void debug(String message, Object... args) {
        log.debug(message, args);
    }
}