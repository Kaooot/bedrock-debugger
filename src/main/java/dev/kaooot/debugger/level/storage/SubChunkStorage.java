package dev.kaooot.debugger.level.storage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtUtils;
import org.cloudburstmc.protocol.common.util.VarInts;
import dev.kaooot.debugger.level.storage.bitarray.BitArray;
import dev.kaooot.debugger.level.storage.bitarray.BitArrayVersion;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class SubChunkStorage<V> {

    private static final int SIZE = 4096;

    @Getter
    private final List<V> palette = new ReferenceArrayList<>(16);

    @Getter
    private BitArray bitArray;

    public SubChunkStorage(V firstValue, BitArrayVersion version) {
        this.bitArray = version.createPalette(SubChunkStorage.SIZE);
        this.palette.add(firstValue);
    }

    public SubChunkStorage(V firstValue) {
        this(firstValue, BitArrayVersion.PALETTED_1);
    }

    public V get(int index) {
        return this.palette.get(this.bitArray.get(index));
    }

    public void set(int index, V value) {
        final int paletteIndex = this.indexAt(value);
        this.bitArray.set(index, paletteIndex);
    }

    public boolean isEmpty() {
        if (this.palette.size() == 1) {
            return true;
        }

        for (int word : this.bitArray.getWords()) {
            if (Integer.toUnsignedLong(word) != 0L) {
                return false;
            }
        }
        return true;
    }

    public void deserializeNetwork(ByteBuf buffer, Function<Integer, V> deserializer) {
        final short header = buffer.readUnsignedByte();
        final boolean isRuntime = (header & 1) != 0;
        final BitArrayVersion version = getVersionFromHeader(header);

        if (version.equals(BitArrayVersion.PALETTED_0)) {
            this.bitArray = version.createPalette(SubChunkStorage.SIZE, null);
            this.palette.clear();
            this.palette.add(deserializer.apply(buffer.readIntLE()));
            this.onResize(BitArrayVersion.PALETTED_2);
            return;
        }

        final int wordCount = version.computeWordCountForSize(SIZE);
        try {
            final int[] words = new int[wordCount];
            for (int i = 0; i < wordCount; i++) {
                words[i] = buffer.readIntLE();
            }

            this.bitArray = version.createPalette(SIZE, words);
            this.palette.clear();

            final int size = VarInts.readInt(buffer);

            for (int i = 0; i < size; i++) {
                if (isRuntime) {
                    final int runtimeId = VarInts.readInt(buffer);
                    final V value = deserializer.apply(runtimeId);
                    this.palette.add(value);
                } else {
                    try (final ByteBufInputStream bufInputStream = new ByteBufInputStream(buffer);
                         final NBTInputStream inputStream = NbtUtils.createNetworkReader(
                             bufInputStream)) {
                        inputStream.readTag();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (NegativeArraySizeException e) {
            throw new IllegalStateException(
                "Invalid wordCount. header: " + header + ", " +
                    "isRuntime: " + isRuntime + ", version: " + version, e
            );
        }
    }

    private int indexAt(V value) {
        int index = this.palette.indexOf(value);

        if (index != -1) {
            return index;
        }

        index = this.palette.size();

        this.palette.add(value);
        final BitArrayVersion version = this.bitArray.getVersion();
        if (index > version.getMaxBlockValue()) {
            final BitArrayVersion next = version.getNext();

            if (next != null) {
                this.onResize(next);
            }
        }
        return index;
    }

    private void onResize(BitArrayVersion version) {
        final BitArray newArray = version.createPalette(SubChunkStorage.SIZE);

        for (int i = 0; i < SubChunkStorage.SIZE; i++) {
            newArray.set(i, this.bitArray.get(i));
        }

        this.bitArray = newArray;
    }

    private static BitArrayVersion getVersionFromHeader(short header) {
        return BitArrayVersion.fromHeader(header >> 1, true);
    }

    public static int getCopyLastFlagHeader() {
        return (0x7f << 1) | 1;
    }
}