package dev.kaooot.debugger.api.forms;

import java.util.function.Consumer;
import lombok.Getter;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Getter
public class FormListener<R> {

    private Consumer<R> responseConsumer = r -> {
    };
    private Consumer<Void> closeConsumer = v -> {
    };

    public void onResponse(Consumer<R> consumer) {
        this.responseConsumer = consumer;
    }

    public void onClose(Consumer<Void> consumer) {
        this.closeConsumer = consumer;
    }
}