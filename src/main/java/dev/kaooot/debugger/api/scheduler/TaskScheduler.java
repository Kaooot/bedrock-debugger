package dev.kaooot.debugger.api.scheduler;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class TaskScheduler {

    private final Int2ObjectArrayMap<Task> tasks = new Int2ObjectArrayMap<>();

    public Task schedule(Runnable runnable, int period, int delay) {
        final Task task = new Task(runnable, period, delay);
        task.start();
        this.tasks.put(this.tasks.size() + 1, task);
        return task;
    }

    public Task schedule(Runnable runnable, int period) {
        return this.schedule(runnable, period, 0);
    }

    public void cancelTask(int taskId) {
        if (this.tasks.containsKey(taskId)) {
            final Task task = this.tasks.get(taskId);
            task.cancel();
            this.tasks.remove(taskId);
        }
    }
}