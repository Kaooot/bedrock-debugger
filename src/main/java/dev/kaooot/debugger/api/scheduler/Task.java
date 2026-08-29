package dev.kaooot.debugger.api.scheduler;

import java.util.concurrent.ScheduledFuture;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

    private final ScheduledFuture<?> future;

    public void cancel() {
        this.future.cancel(false);
    }

    public boolean isCancelled() {
        return this.future.isCancelled();
    }
}