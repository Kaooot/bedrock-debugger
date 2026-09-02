package dev.kaooot.debugger.core.blockpalette;

import dev.kaooot.debugger.core.memory.ProcessMemory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public final class BlockLayoutDeriver {

    private static final int REACH = 512;
    private static final int MAX_LEAD = 1024;
    private static final int MINIMUM_ROUND_TRIPS = 8;
    private static final int SAMPLE = 800;

    private final ProcessMemory process;

    public BlockLayoutDeriver(final ProcessMemory process) {
        this.process = process;
    }

    @Value
    public static class NamedThing {
        long address;
        String name;
    }

    @Value
    public static class Block {
        String name;
        long address;
    }

    @Value
    public static class BlockLayout {
        int nameInsideLegacy;
        int defaultStatePointer;
        int blockLegacyPointer;
        int agreed;
    }

    public List<NamedThing> scanNamedThings() {
        final List<NamedThing> found = new ArrayList<>();
        final byte[] heap = new byte[256];
        this.process.forEachChunk(8 * 1024 * 1024, HashedString.SIZE, (base, window, length) -> {
            for (int i = 0; i + HashedString.SIZE <= length; i += 8) {
                final String name = HashedString.readVerified(this.process, window, i, heap);
                if (name != null && isNamespaced(name)) {
                    found.add(new NamedThing(base + i, name));
                }
            }
        });
        return found;
    }

    public BlockLayout deriveBlockLayout(final List<NamedThing> names) {
        if (names.isEmpty()) {
            return null;
        }
        final Map<Key, Integer> votes = new HashMap<>();
        final byte[] block = new byte[MAX_LEAD + REACH + 16];
        final byte[] state = new byte[REACH + 16];

        final int step = Math.max(1, names.size() / SAMPLE);
        for (int index = 0; index < names.size(); index += step) {
            final long name = names.get(index).getAddress();
            final int have = this.process.readClipped(name, block, REACH + 8);
            for (int df = 0; df + 8 <= have; df += 8) {
                final long stateAddress = ProcessMemory.getLong(block, df);
                if (!this.looksLikeObject(stateAddress)) {
                    continue;
                }
                final int stateHave = this.process.readClipped(stateAddress, state, REACH + 8);
                for (int backAt = 0; backAt + 8 <= stateHave; backAt += 8) {
                    final long owner = ProcessMemory.getLong(state, backAt);
                    if (owner < ProcessMemory.MIN_POINTER || name - owner < 0 ||
                        name - owner > MAX_LEAD) {
                        continue;
                    }
                    if (!this.looksLikeObject(owner)) {
                        continue;
                    }
                    final Key key = new Key((int) (name - owner), df, backAt);
                    votes.merge(key, 1, Integer::sum);
                }
            }
        }

        if (votes.isEmpty()) {
            return null;
        }
        Key best = null;
        int bestVotes = 0;
        for (final Map.Entry<Key, Integer> entry : votes.entrySet()) {
            if (entry.getValue() > bestVotes) {
                bestVotes = entry.getValue();
                best = entry.getKey();
            }
        }
        if (best == null || bestVotes < MINIMUM_ROUND_TRIPS) {
            return null;
        }
        return new BlockLayout(best.getLead(), best.getDf(), best.getBackAt(), bestVotes);
    }

    public List<Block> findBlocks(final List<NamedThing> names, final Layout layout) {
        final Map<Long, Block> blocks = new LinkedHashMap<>();
        for (final NamedThing named : names) {
            final long nameAddress = named.getAddress();
            if (nameAddress < layout.nameInsideLegacy) {
                continue;
            }
            final long legacy = nameAddress - layout.nameInsideLegacy;
            if (!this.looksLikeObject(legacy)) {
                continue;
            }
            final long stateAddress =
                this.process.readPointer(nameAddress + layout.defaultStatePointer);
            if (stateAddress < ProcessMemory.MIN_POINTER || !this.process.isMapped(stateAddress)) {
                continue;
            }
            if (this.process.readPointer(stateAddress + layout.blockLegacyPointer) != legacy) {
                continue;
            }
            blocks.putIfAbsent(legacy, new Block(named.getName(), legacy));
        }
        return new ArrayList<>(blocks.values());
    }

    private boolean looksLikeObject(final long address) {
        if (address < ProcessMemory.MIN_POINTER || !this.process.isMapped(address)) {
            return false;
        }
        final long vtable = this.process.readPointer(address);
        return vtable >= ProcessMemory.MIN_POINTER && this.process.isMapped(vtable);
    }

    private static boolean isNamespaced(final String name) {
        final int colon = name.indexOf(':');
        if (colon < 1 || colon == name.length() - 1) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (i == colon) {
                continue;
            }
            final boolean ok =
                (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '.';
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    @Value
    private static class Key {
        int lead;
        int df;
        int backAt;
    }
}