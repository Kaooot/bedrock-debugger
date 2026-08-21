package dev.kaooot.debugger.level.storage.bitarray;

import lombok.Getter;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Getter
public enum BitArrayVersion {

    PALETTED_16(16, 2, null),
    PALETTED_8(8, 4, PALETTED_16),
    PALETTED_6(6, 5, PALETTED_8), // 2 bits of padding per word
    PALETTED_5(5, 6, PALETTED_6), // 2 bits of padding per word
    PALETTED_4(4, 8, PALETTED_5),
    PALETTED_3(3, 10, PALETTED_4), // 2 bits of padding per word
    PALETTED_2(2, 16, PALETTED_3),
    PALETTED_1(1, 32, PALETTED_2),
    PALETTED_0(0, 0, PALETTED_1);

    private final byte bits;
    private final byte blocksPerWord;
    private final int maxBlockValue;
    private final BitArrayVersion next;

    private static final BitArrayVersion[] VALUES = values();

    BitArrayVersion(int bits, int blocksPerWord, BitArrayVersion next) {
        this.bits = (byte) bits;
        this.blocksPerWord = (byte) blocksPerWord;
        this.maxBlockValue = (1 << this.bits) - 1;
        this.next = next;
    }

    public static BitArrayVersion fromHeader(int version, boolean read) {
        for (final BitArrayVersion value : BitArrayVersion.VALUES) {
            if ((!read && value.blocksPerWord <= version) || (read && value.bits == version)) {
                return value;
            }
        }
        throw new RuntimeException("Invalid palette version detected: " + version);
    }

    public BitArray createPalette(int size, int[] words) {
        if (this.equals(BitArrayVersion.PALETTED_3) || this.equals(BitArrayVersion.PALETTED_5) ||
            this.equals(BitArrayVersion.PALETTED_6)) {
            return new PaddedBitArray(this, size, words);
        } else if (this.equals(PALETTED_0)) {
            return new SingletonBitArray();
        } else {
            return new Pow2BitArray(this, size, words);
        }
    }

    public BitArray createPalette(int size) {
        return this.createPalette(size, new int[this.computeWordCountForSize(size)]);
    }

    public int computeWordCountForSize(int size) {
        return this.ceil((float) size / this.blocksPerWord);
    }

    private int ceil(float value) {
        final int truncated = (int) value;
        return value > truncated ? truncated + 1 : truncated;
    }
}