package dev.kaooot.debugger.api.command;

import lombok.Getter;
import lombok.Setter;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Getter
@Setter
public abstract class Command<T> {

    private String name = "";
    private String[] args = new String[0];

    public abstract void execute(String command, String[] args, T proxy);
}