package dev.kaooot.debugger.core.blockpalette;

import dev.kaooot.debugger.core.memory.ProcessMemory;
import java.nio.charset.StandardCharsets;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public final class HashedString {

    public static final int HASH_OFFSET = 0;
    public static final int TEXT_OFFSET = 8;
    public static final int LENGTH_OFFSET = 24;
    public static final int CAPACITY_OFFSET = 32;

    public static final int SIZE = 48;

    private static final int MINIMUM_NAME_LENGTH = 11;
    private static final int MAXIMUM_NAME_LENGTH = 64;

    private HashedString() {
    }

    public static long fnv1(final String text) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < text.length(); i++) {
            hash *= 0x100000001b3L;
            hash ^= (byte) text.charAt(i);
        }
        return hash;
    }

    public static String readVerified(final ProcessMemory process, final byte[] buffer, final int at,
                                      final byte[] heapScratch) {
        return readVerified(process, buffer, at, heapScratch, MINIMUM_NAME_LENGTH, MAXIMUM_NAME_LENGTH);
    }

    public static String readVerified(final ProcessMemory process, final byte[] buffer, final int at,
                                      final byte[] heapScratch, final int minimum, final int maximum) {
        if (at + CAPACITY_OFFSET + 8 > buffer.length) {
            return null;
        }
        final long hash = ProcessMemory.getLong(buffer, at + HASH_OFFSET);
        if (hash == 0) {
            return null;
        }
        final long length = ProcessMemory.getLong(buffer, at + LENGTH_OFFSET);
        final long capacity = ProcessMemory.getLong(buffer, at + CAPACITY_OFFSET);
        if (length < minimum || length > maximum) {
            return null;
        }
        if (capacity < length || (capacity + 1) % 16 != 0) {
            return null;
        }
        final String text = capacity == 15
            ? new String(buffer, at + TEXT_OFFSET, (int) length, StandardCharsets.US_ASCII)
            : readFromHeap(process, ProcessMemory.getLong(buffer, at + TEXT_OFFSET), (int) length, heapScratch);
        if (text == null) {
            return null;
        }
        return fnv1(text) == hash ? text : null;
    }

    private static String readFromHeap(final ProcessMemory process, final long pointer, final int length,
                                       final byte[] scratch) {
        if (pointer < ProcessMemory.MIN_POINTER || length > scratch.length || !process.isMapped(pointer)) {
            return null;
        }
        if (!process.tryRead(pointer, scratch, length)) {
            return null;
        }
        for (int i = 0; i < length; i++) {
            if (scratch[i] < 0x20 || scratch[i] > 0x7E) {
                return null;
            }
        }
        return new String(scratch, 0, length, StandardCharsets.US_ASCII);
    }
}