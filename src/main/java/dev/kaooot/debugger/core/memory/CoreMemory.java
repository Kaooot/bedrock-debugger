package dev.kaooot.debugger.core.memory;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class CoreMemory implements AutoCloseable {

    public static final String LEVEL_SOUND_EVENT_ANCHOR = "item.use.on";
    public static final String PARTICLE_TYPE_ANCHOR = "none";

    private static final Pattern IDENTIFIER = Pattern.compile("[a-z][a-z0-9_.]*");
    private static final int DEFAULT_MAX_GAP = 2048;

    private final FileChannel channel;
    private final ByteBuffer image;
    private final long imageBase;
    private final long[] sectionVirtualAddress;
    private final long[] sectionVirtualSize;
    private final long[] sectionRawAddress;
    private final long[] sectionRawSize;

    private final LongArrayList leaInstruction = new LongArrayList();
    private final IntArrayList leaTarget = new IntArrayList();

    public CoreMemory(Path executable) throws IOException {
        this.channel = FileChannel.open(executable, StandardOpenOption.READ);
        final MappedByteBuffer buffer = this.channel.map(FileChannel.MapMode.READ_ONLY, 0,
            this.channel.size());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.image = buffer;

        final int peOffset = buffer.getInt(0x3c);
        final int sectionCount = buffer.getShort(peOffset + 6) & 0xffff;
        final int sectionTable = peOffset + 24 + (buffer.getShort(peOffset + 20) & 0xffff);
        this.imageBase = buffer.getLong(peOffset + 24 + 24);

        this.sectionVirtualSize = new long[sectionCount];
        this.sectionVirtualAddress = new long[sectionCount];
        this.sectionRawSize = new long[sectionCount];
        this.sectionRawAddress = new long[sectionCount];
        long textStart = 0;
        long textEnd = 0;
        for (int i = 0; i < sectionCount; i++) {
            final int header = sectionTable + i * 40;
            this.sectionVirtualSize[i] = buffer.getInt(header + 8) & 0xffffffffL;
            this.sectionVirtualAddress[i] = buffer.getInt(header + 12) & 0xffffffffL;
            this.sectionRawSize[i] = buffer.getInt(header + 16) & 0xffffffffL;
            this.sectionRawAddress[i] = buffer.getInt(header + 20) & 0xffffffffL;
            final byte[] name = new byte[8];
            buffer.get(header, name);
            if (new String(name, StandardCharsets.US_ASCII).startsWith(".text")) {
                textStart = this.sectionRawAddress[i];
                textEnd = textStart + this.sectionRawSize[i];
            }
        }
        this.indexStringLeas(textStart, textEnd);
    }

    public List<String> getLevelSoundEvents() {
        return this.getEnumValues(LEVEL_SOUND_EVENT_ANCHOR);
    }

    public List<String> getParticleTypes() {
        return this.getEnumValues(PARTICLE_TYPE_ANCHOR);
    }

    public List<String> getEnumValues(String firstMember) {
        List<String> best = new ArrayList<>();
        for (int i = 0; i < this.leaInstruction.size(); i++) {
            if (firstMember.equals(this.readString(this.leaTarget.getInt(i)))) {
                final List<String> run = this.walk(i);
                if (run.size() > best.size()) {
                    best = run;
                }
            }
        }
        return best;
    }

    private List<String> walk(int startIndex) {
        final List<String> values = new ArrayList<>();
        final Set<String> seen = new HashSet<>();
        values.add(this.readString(this.leaTarget.getInt(startIndex)));
        seen.add(values.getFirst());

        long current = this.leaInstruction.getLong(startIndex);
        int i = startIndex + 1;
        while (i < this.leaInstruction.size()) {
            int next = -1;
            for (int k = i; k < this.leaInstruction.size()
                && this.leaInstruction.getLong(k) - current <= DEFAULT_MAX_GAP; k++) {
                final String value = this.readString(this.leaTarget.getInt(k));
                if (value != null && IDENTIFIER.matcher(value).matches() && !seen.contains(value)) {
                    next = k;
                    break;
                }
            }
            if (next < 0) {
                break;
            }
            final String value = this.readString(this.leaTarget.getInt(next));
            values.add(value);
            seen.add(value);
            current = this.leaInstruction.getLong(next);
            i = next + 1;
        }
        return values;
    }

    private void indexStringLeas(long textStart, long textEnd) {
        for (int offset = (int) textStart; offset + 7 <= textEnd; offset++) {
            final int rex = this.image.get(offset) & 0xff;
            if (rex < 0x48 || rex > 0x4f || (this.image.get(offset + 1) & 0xff) != 0x8d) {
                continue;
            }
            if ((this.image.get(offset + 2) & 0xc7) != 0x05) {
                continue;
            }
            final long instruction = this.fileToVirtual(offset);
            final long targetFile =
                this.virtualToFile(instruction + 7 + this.image.getInt(offset + 3));
            if (targetFile < 0 || this.readString(targetFile) == null) {
                continue;
            }
            this.leaInstruction.add(instruction);
            this.leaTarget.add((int) targetFile);
        }
    }

    private String readString(long fileOffset) {
        final int first = this.image.get((int) fileOffset) & 0xff;
        final int second = this.image.get((int) (fileOffset + 1)) & 0xff;
        if (first < 0x20 || first >= 0x7f || second < 0x20 || second >= 0x7f) {
            return null;
        }
        final StringBuilder builder = new StringBuilder();
        int offset = (int) fileOffset;
        int character;
        while ((character = this.image.get(offset++) & 0xff) >= 0x20 && character < 0x7f
            && builder.length() < 96) {
            builder.append((char) character);
        }
        return character == 0 ? builder.toString() : null;
    }

    private long fileToVirtual(long fileOffset) {
        for (int i = 0; i < this.sectionRawAddress.length; i++) {
            if (fileOffset >= this.sectionRawAddress[i]
                && fileOffset < this.sectionRawAddress[i] + this.sectionRawSize[i]) {
                return this.imageBase + this.sectionVirtualAddress[i]
                    + (fileOffset - this.sectionRawAddress[i]);
            }
        }
        return -1;
    }

    private long virtualToFile(long virtualAddress) {
        final long relative = virtualAddress - this.imageBase;
        for (int i = 0; i < this.sectionVirtualAddress.length; i++) {
            if (relative >= this.sectionVirtualAddress[i]
                && relative < this.sectionVirtualAddress[i] + this.sectionVirtualSize[i]) {
                final long offset = relative - this.sectionVirtualAddress[i];
                if (offset < this.sectionRawSize[i]) {
                    return this.sectionRawAddress[i] + offset;
                }
            }
        }
        return -1;
    }

    @Override
    public void close() throws IOException {
        this.channel.close();
    }
}