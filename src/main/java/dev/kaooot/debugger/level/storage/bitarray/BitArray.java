package dev.kaooot.debugger.level.storage.bitarray;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public interface BitArray {

    void set(int index, int value);

    int get(int index);

    int getSize();

    int[] getWords();

    BitArrayVersion getVersion();

    BitArray copy();
}