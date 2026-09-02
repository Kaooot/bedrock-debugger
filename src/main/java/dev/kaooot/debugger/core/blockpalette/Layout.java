package dev.kaooot.debugger.core.blockpalette;

import dev.kaooot.debugger.core.memory.ProcessMemory;
import java.nio.charset.StandardCharsets;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public final class Layout {

    public int nameInsideLegacy = -1;
    public int defaultStatePointer = -1;
    public int blockLegacyPointer = -1;
    public int blockStateNbt = -1;

    public static final int MAP_NODE_LEFT = 0;
    public static final int MAP_NODE_PARENT = 8;
    public static final int MAP_NODE_RIGHT = 16;
    public static final int MAP_NODE_FLAGS = 24;
    public static final int MAP_NODE_KEY = 32;
    public static final int MAP_NODE_VTABLE = 64;
    public static final int MAP_NODE_PAYLOAD = 72;

    public static final int MINIMUM_TAG_LENGTH = 3;
    public static final int MAXIMUM_TAG_LENGTH = 64;

    public boolean blockLayoutMeasured() {
        return this.nameInsideLegacy >= 0 && this.defaultStatePointer >= 0 &&
            this.blockLegacyPointer >= 0;
    }

    public static String readStdString(final ProcessMemory process, final long at,
                                       final byte[] textScratch) {
        final byte[] header = new byte[32];
        if (!process.tryRead(at, header, header.length)) {
            return null;
        }
        final long length = ProcessMemory.getLong(header, 16);
        final long capacity = ProcessMemory.getLong(header, 24);
        if (length == 0 || length > capacity || length >= textScratch.length) {
            return null;
        }
        if (capacity < 16) {
            return new String(header, 0, (int) length, StandardCharsets.US_ASCII);
        }
        final long pointer = ProcessMemory.getLong(header, 0);
        if (pointer < ProcessMemory.MIN_POINTER ||
            !process.tryRead(pointer, textScratch, (int) length)) {
            return null;
        }
        return new String(textScratch, 0, (int) length, StandardCharsets.US_ASCII);
    }
}