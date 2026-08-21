package dev.kaooot.debugger.level.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.nbt.NbtMap;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Getter
@RequiredArgsConstructor
public class Block {

    public static Block AIR;

    private final int blockRuntimeId;
    private final NbtMap state;
    private final boolean hashed;
}