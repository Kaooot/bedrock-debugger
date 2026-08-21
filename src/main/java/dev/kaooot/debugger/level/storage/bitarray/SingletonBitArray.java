package dev.kaooot.debugger.level.storage.bitarray;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class SingletonBitArray implements BitArray {

    @Override
    public void set(int index, int value) {

    }

    @Override
    public int get(int index) {
        return 0;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public int[] getWords() {
        return new int[0];
    }

    @Override
    public BitArrayVersion getVersion() {
        return BitArrayVersion.PALETTED_0;
    }

    @Override
    public BitArray copy() {
        return new SingletonBitArray();
    }
}