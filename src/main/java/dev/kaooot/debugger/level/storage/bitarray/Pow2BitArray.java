package dev.kaooot.debugger.level.storage.bitarray;

import java.util.Arrays;
import lombok.Getter;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class Pow2BitArray implements BitArray {

    @Getter
    private final int[] words;
    @Getter
    private final BitArrayVersion version;
    @Getter
    private final int size;

    Pow2BitArray(BitArrayVersion version, int size, int[] words) {
        this.version = version;
        this.size = size;
        this.words = words;

        final int expWordCount = this.ceil((float) size / version.getBlocksPerWord());

        if (words.length != expWordCount) {
            throw new RuntimeException(
                "The word count is invalid. Detected: " + words.length + " but expected: " +
                    expWordCount
            );
        }
    }

    @Override
    public void set(int index, int value) {
        final int bitIndex = index * this.version.getBits();
        final int arrayIndex = bitIndex >> 5;
        final int offset = bitIndex & 31;

        this.words[arrayIndex] = this.words[arrayIndex] &
            ~(this.version.getMaxBlockValue() << offset) |
            (value & this.version.getMaxBlockValue()) << offset;
    }

    @Override
    public int get(int index) {
        final int bitIndex = index * this.version.getBits();
        final int arrayIndex = bitIndex >> 5;
        final int offset = bitIndex & 31;

        return this.words[arrayIndex] >>> offset & this.version.getMaxBlockValue();
    }

    @Override
    public BitArray copy() {
        return new Pow2BitArray(this.version, this.size,
            Arrays.copyOf(this.words, this.words.length));
    }

    private int ceil(float value) {
        final int truncated = (int) value;
        return value > truncated ? truncated + 1 : truncated;
    }
}