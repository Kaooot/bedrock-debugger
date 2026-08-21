package dev.kaooot.debugger.api.scheduler;

import java.util.Timer;
import java.util.TimerTask;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

    private final Runnable runnable;
    private final int period;
    private final int delay;
    private final Timer timer = new Timer();

    public void start() {
        if(this.period == 0){
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Task.this.runnable.run();
                }
            }, this.delay * 50L);
        } else {
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Task.this.runnable.run();
                }
            }, this.delay * 50L, this.period * 50L);
        }
    }

    public void cancel() {
        this.timer.cancel();
    }
}