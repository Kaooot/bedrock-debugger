package dev.kaooot.debugger.api.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class TaskScheduler {

    private static final long MILLIS_PER_TICK = 50L;
    private static final int POOL_SIZE =
        Math.max(2, Runtime.getRuntime().availableProcessors() / 2);

    private final AtomicInteger threadCounter = new AtomicInteger();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(
        POOL_SIZE,
        runnable -> {
            final Thread thread = new Thread(
                runnable, "task-scheduler-" + this.threadCounter.incrementAndGet()
            );
            thread.setDaemon(true);
            return thread;
        }
    );
    private final Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger taskIdCounter = new AtomicInteger();

    public Task schedule(Runnable runnable, int period, int delay) {
        final Runnable safeRunnable = () -> {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        };
        final long initialDelay = (long) delay * MILLIS_PER_TICK;
        final ScheduledFuture<?> future = period == 0
            ? this.executor.schedule(safeRunnable, initialDelay, TimeUnit.MILLISECONDS)
            : this.executor.scheduleWithFixedDelay(
                safeRunnable, initialDelay, (long) period * MILLIS_PER_TICK, TimeUnit.MILLISECONDS
            );
        final Task task = new Task(future);
        if (period != 0) {
            this.tasks.put(this.taskIdCounter.incrementAndGet(), task);
        }
        return task;
    }

    public Task schedule(Runnable runnable, int period) {
        return this.schedule(runnable, period, 0);
    }

    public void cancelTask(int taskId) {
        final Task task = this.tasks.remove(taskId);
        if (task != null) {
            task.cancel();
        }
    }

    public void shutdown() {
        this.executor.shutdownNow();
    }
}