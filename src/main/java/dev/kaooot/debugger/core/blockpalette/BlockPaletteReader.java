package dev.kaooot.debugger.core.blockpalette;

import dev.kaooot.debugger.core.blockpalette.BlockLayoutDeriver.Block;
import dev.kaooot.debugger.core.blockpalette.BlockStateReader.State;
import dev.kaooot.debugger.core.blockpalette.BlockStateReader.StateProperty;
import dev.kaooot.debugger.core.memory.ProcessMemory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public final class BlockPaletteReader {

    @Value
    public static class VectorHeader {
        long begin;
        long end;
        long capacity;
        int count;
    }

    @Value
    public static class PaletteEntry {
        int index;
        long address;
        String name;
        long nameHash;
        int version;
        List<StateProperty> states;
    }

    private final ProcessMemory process;
    private final Layout layout;

    public BlockPaletteReader(final ProcessMemory process, final Layout layout) {
        this.process = process;
        this.layout = layout;
    }

    public VectorHeader findPalette(final List<Block> blocks) {
        final Set<Long> defaults = new HashSet<>();
        for (final Block block : blocks) {
            final long state = this.process.readPointer(
                block.getAddress() + this.layout.nameInsideLegacy + this.layout.defaultStatePointer);
            if (state >= ProcessMemory.MIN_POINTER) {
                defaults.add(state);
            }
        }
        if (defaults.isEmpty()) {
            return null;
        }

        final boolean debug = System.getProperty("palette.debug") != null;
        long bestHeld = 0;
        long bestCount = 0;
        int seedsTried = 0;
        for (final long seed : defaults) {
            if (seedsTried++ >= 24) {
                break;
            }
            for (final long site : this.findPointersTo(Set.of(seed))) {
                final long[] span = this.spanAround(site);
                final long begin = span[0];
                final long end = span[1];
                final long count = (end - begin) / 8;
                if (count < defaults.size() * 0.5) {
                    continue;
                }
                final Set<Long> held = new HashSet<>();
                for (long slot = begin; slot < end; slot += 8) {
                    held.add(this.process.readPointer(slot));
                }
                final long have = defaults.stream().filter(held::contains).count();
                if (have > bestHeld) {
                    bestHeld = have;
                    bestCount = count;
                }
                if (have >= defaults.size() * 0.98) {
                    final VectorHeader header = this.header(begin, end);
                    if (debug) {
                        System.out.printf("    palette run 0x%X..0x%X count=%d holds %d/%d defaults, header=%s%n",
                            begin, end, count, have, defaults.size(), header);
                    }
                    if (header != null) {
                        return header;
                    }
                }
            }
        }
        if (debug) {
            System.out.printf("    best run held %d/%d defaults, count=%d%n",
                bestHeld, defaults.size(), bestCount);
        }
        return null;
    }

    private long[] spanAround(final long site) {
        long begin = site;
        while (begin >= 8 && this.isState(this.process.readPointer(begin - 8))) {
            begin -= 8;
        }
        long end = site + 8;
        while (this.isState(this.process.readPointer(end))) {
            end += 8;
        }
        return new long[] {begin, end};
    }

    private VectorHeader header(final long walked, final long walkedEnd) {
        final Set<Long> fronts = new HashSet<>();
        for (long slot = walked; slot < walkedEnd && fronts.size() < 64; slot += 8) {
            fronts.add(slot);
        }
        for (final long site : this.findPointersTo(fronts)) {
            final long begin = this.process.readPointer(site);
            final long end = this.process.readPointer(site + 8);
            final long capacity = this.process.readPointer(site + 16);
            if (!fronts.contains(begin) || end <= begin || end > walkedEnd || capacity < end) {
                continue;
            }
            if ((end - begin) % 8 != 0) {
                continue;
            }
            return new VectorHeader(begin, end, capacity, (int) ((end - begin) / 8));
        }
        return null;
    }

    private boolean isState(final long address) {
        if (address < ProcessMemory.MIN_POINTER || !this.process.isMapped(address)) {
            return false;
        }
        final long owner = this.process.readPointer(address + this.layout.blockLegacyPointer);
        return owner >= ProcessMemory.MIN_POINTER && this.process.isMapped(owner)
            && this.process.readPointer(owner) >= ProcessMemory.MIN_POINTER;
    }

    public List<PaletteEntry> read(final VectorHeader header) {
        final byte[] slots = new byte[header.getCount() * 8];
        if (this.process.readClipped(header.getBegin(), slots, slots.length) < slots.length) {
            return List.of();
        }
        final List<Long> addresses = new ArrayList<>(header.getCount());
        for (int i = 0; i < header.getCount(); i++) {
            addresses.add(ProcessMemory.getLong(slots, i * 8));
        }

        final BlockStateReader reader = new BlockStateReader(this.process, this.layout, addresses);

        final byte[] legacy = new byte[this.layout.nameInsideLegacy + HashedString.SIZE];
        final byte[] heap = new byte[256];
        final List<PaletteEntry> entries = new ArrayList<>(header.getCount());
        for (int i = 0; i < header.getCount(); i++) {
            final long address = addresses.get(i);
            String name = "";
            long hash = 0;
            int version = 0;
            List<StateProperty> states = List.of();

            if (this.process.isMapped(address)) {
                final long owner = this.process.readPointer(address + this.layout.blockLegacyPointer);
                if (this.process.isMapped(owner) && this.process.readClipped(owner, legacy, legacy.length) >= legacy.length) {
                    final String read = HashedString.readVerified(this.process, legacy, this.layout.nameInsideLegacy, heap);
                    if (read != null) {
                        name = read;
                        hash = ProcessMemory.getLong(legacy, this.layout.nameInsideLegacy);
                    }
                }
                final State state = reader.read(address);
                version = state.getVersion();
                states = state.getProperties();
            }

            entries.add(new PaletteEntry(i, address, name, hash, version, states));
        }
        return entries;
    }

    private List<Long> findPointersTo(final Set<Long> targets) {
        final List<Long> sites = new ArrayList<>();
        this.process.forEachChunk(8 * 1024 * 1024, 0, (base, window, length) -> {
            for (int i = 0; i + 8 <= length; i += 8) {
                final long value = ProcessMemory.getLong(window, i);
                if (value >= ProcessMemory.MIN_POINTER && targets.contains(value)) {
                    sites.add(base + i);
                }
            }
        });
        return sites;
    }
}