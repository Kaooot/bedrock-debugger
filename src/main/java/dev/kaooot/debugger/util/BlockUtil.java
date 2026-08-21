package dev.kaooot.debugger.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.TreeMap;
import lombok.experimental.UtilityClass;
import org.cloudburstmc.nbt.NBTOutputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtUtils;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@UtilityClass
public class BlockUtil {

    private static final int FNV1_32_INIT = 0x811c9dc5;
    private static final int FNV1_32_PRIME = 0x01000193;

    public int fnv1a_32(byte[] data) {
        int hash = FNV1_32_INIT;
        for (final byte datum : data) {
            hash ^= (datum & 0xff);
            hash *= FNV1_32_PRIME;
        }
        return hash;
    }

    public int createHash(String blockName, NbtMap states) {
        if (blockName.equals("minecraft:unknown")) {
            return -2; // This is special case
        }
        // Order required
        final NbtMap tag = NbtMap.builder()
            .putString("name", blockName)
            .putCompound("states", NbtMap.fromMap(new TreeMap<>(states)))
            .build();
        final byte[] bytes;
        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream();
             final NBTOutputStream outputStream = NbtUtils.createWriterLE(stream)) {
            outputStream.writeTag(tag);
            bytes = stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return BlockUtil.fnv1a_32(bytes);
    }
}