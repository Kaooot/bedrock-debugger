package dev.kaooot.debugger.level.storage.bitarray;

import java.util.Arrays;
import lombok.Getter;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class PaddedBitArray implements BitArray {

    @Getter
    private final int[] words;
    @Getter
    private final BitArrayVersion version;
    @Getter
    private final int size;

    PaddedBitArray(BitArrayVersion version, int size, int[] words) {
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
        final int arrayIndex = index / this.version.getBlocksPerWord();
        final int offset = index % this.version.getBlocksPerWord() * this.version.getBits();

        this.words[arrayIndex] = this.words[arrayIndex] &
            ~(this.version.getMaxBlockValue() << offset) | (value & this.version.getMaxBlockValue())
            << offset;
    }

    @Override
    public int get(int index) {
        final int arrayIndex = index / this.version.getBlocksPerWord();
        final int offset = (index % this.version.getBlocksPerWord()) * this.version.getBits();

        return (this.words[arrayIndex] >>> offset) & this.version.getMaxBlockValue();
    }

    @Override
    public BitArray copy() {
        return new PaddedBitArray(this.version, this.size,
            Arrays.copyOf(this.words, this.words.length));
    }

    private int ceil(float value) {
        final int truncated = (int) value;
        return value > truncated ? truncated + 1 : truncated;
    }
}