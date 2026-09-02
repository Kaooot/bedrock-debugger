package dev.kaooot.debugger.core.blockpalette;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kaooot.debugger.core.blockpalette.BlockLayoutDeriver.Block;
import dev.kaooot.debugger.core.blockpalette.BlockLayoutDeriver.BlockLayout;
import dev.kaooot.debugger.core.blockpalette.BlockLayoutDeriver.NamedThing;
import dev.kaooot.debugger.core.blockpalette.BlockPaletteReader.PaletteEntry;
import dev.kaooot.debugger.core.blockpalette.BlockPaletteReader.VectorHeader;
import dev.kaooot.debugger.core.blockpalette.BlockStateReader.StateProperty;
import dev.kaooot.debugger.core.memory.ProcessMemory;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.experimental.UtilityClass;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@UtilityClass
public final class BlockPaletteExtractor {

    public List<PaletteEntry> extractWithRetry(final long pid, final long graceMillis,
                                               final long timeoutMillis,
                                               final long retryMillis) {
        sleep(graceMillis);
        final long deadline = System.currentTimeMillis() + timeoutMillis;
        do {
            try (final ProcessMemory process = ProcessMemory.open(pid)) {
                final List<PaletteEntry> palette = extract(process, false);
                if (palette != null && !palette.isEmpty()) {
                    return palette;
                }
            } catch (final RuntimeException ignored) {
            }
            sleep(retryMillis);
        } while (System.currentTimeMillis() < deadline);
        return null;
    }

    public List<PaletteEntry> extract(final ProcessMemory process, final boolean verbose) {
        final Layout layout = new Layout();
        final BlockLayoutDeriver deriver = new BlockLayoutDeriver(process);

        final List<NamedThing> names = deriver.scanNamedThings();
        if (names.isEmpty()) {
            return null;
        }

        final BlockLayout measured = deriver.deriveBlockLayout(names);
        if (measured == null) {
            return null;
        }
        layout.nameInsideLegacy = measured.getNameInsideLegacy();
        layout.defaultStatePointer = measured.getDefaultStatePointer();
        layout.blockLegacyPointer = measured.getBlockLegacyPointer();
        log(verbose, String.format(
            "  block layout: name at +%d, state pointer at name+%d, back pointer at state+%d (%d round trips)",
            layout.nameInsideLegacy, layout.defaultStatePointer, layout.blockLegacyPointer,
            measured.getAgreed()));

        final List<Block> blocks = deriver.findBlocks(names, layout);
        if (blocks.isEmpty()) {
            return null;
        }
        log(verbose, String.format("  blocks found: %,d", blocks.size()));

        layout.blockStateNbt = BlockStateReader.deriveStateNbt(process, layout, blocks);
        if (layout.blockStateNbt < 0) {
            return null;
        }

        final BlockPaletteReader reader = new BlockPaletteReader(process, layout);
        final VectorHeader header = reader.findPalette(blocks);
        if (header == null) {
            return null;
        }

        final List<PaletteEntry> palette = reader.read(header);
        if (verbose) {
            report(palette, header);
        }
        return palette;
    }

    private void report(final List<PaletteEntry> palette, final VectorHeader header) {
        final long unnamed = palette.stream().filter(e -> e.getName().isEmpty()).count();
        final long badHash = palette.stream()
            .filter(e -> !e.getName().isEmpty() && HashedString.fnv1(e.getName()) != e.getNameHash())
            .count();
        System.out.printf("  palette at 0x%X: %,d states, %,d unnamed, %,d name/hash mismatches%n",
            header.getBegin(), palette.size(), unnamed, badHash);
        palette.stream().map(PaletteEntry::getVersion).max(Integer::compareTo).ifPresent(v ->
            System.out.printf("  block palette version %d (%d.%d.%d.%d)%n",
                v, (v >> 24) & 0xFF, (v >> 16) & 0xFF, (v >> 8) & 0xFF, v & 0xFF));
    }

    private void validate(final List<PaletteEntry> palette, final Path reference)
        throws IOException {
        final JsonArray ref;
        try (final Reader in = Files.newBufferedReader(reference, StandardCharsets.UTF_8)) {
            final JsonElement root = new Gson().fromJson(in, JsonElement.class);
            ref = root.isJsonArray() ? root.getAsJsonArray() :
                root.getAsJsonObject().getAsJsonArray("blocks");
        }
        final List<String> ours = palette.stream().map(BlockPaletteExtractor::canonical).toList();
        final List<String> theirs = new ArrayList<>(ref.size());
        for (final JsonElement element : ref) {
            theirs.add(canonicalReference(element.getAsJsonObject()));
        }

        System.out.println();
        System.out.printf("validation against %s: %,d ours vs %,d reference%n",
            reference.getFileName(), ours.size(), theirs.size());

        final int limit = Math.min(ours.size(), theirs.size());
        int matched = 0;
        final List<String> mismatches = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            if (ours.get(i).equals(theirs.get(i))) {
                matched++;
            } else if (mismatches.size() < 20) {
                mismatches.add(
                    String.format("  [%d] ours=%s  ref=%s", i, ours.get(i), theirs.get(i)));
            }
        }
        System.out.printf("  %,d of %,d positions identical%n", matched, limit);
        if (ours.size() != theirs.size()) {
            System.out.printf("  length differs by %d%n", ours.size() - theirs.size());
        }

        final Set<String> refSet = new HashSet<>(theirs);
        final Set<String> ourSet = new HashSet<>(ours);
        final long oursInRef = ours.stream().filter(refSet::contains).count();
        final List<String> oursMissing =
            ours.stream().filter(s -> !refSet.contains(s)).distinct().limit(15).toList();
        final List<String> refMissing =
            theirs.stream().filter(s -> !ourSet.contains(s)).distinct().limit(15).toList();
        System.out.printf(
            "  as sets: %,d of ours present in the reference; %,d of ours absent, %,d of the reference absent from ours%n",
            oursInRef, ours.size() - oursInRef,
            theirs.stream().filter(s -> !ourSet.contains(s)).count());

        if (mismatches.isEmpty() && ours.size() == theirs.size()) {
            System.out.println("  exact match: every block state agrees, in order");
        } else {
            if (!oursMissing.isEmpty()) {
                System.out.println("  ours not in reference (sample):");
                oursMissing.forEach(s -> System.out.println("    " + s));
            }
            if (!refMissing.isEmpty()) {
                System.out.println("  reference not in ours (sample):");
                refMissing.forEach(s -> System.out.println("    " + s));
            }
        }
    }

    private String canonical(final PaletteEntry entry) {
        final StringBuilder builder = new StringBuilder(entry.getName()).append('|');
        for (final StateProperty property : entry.getStates()) {
            builder.append(property.getName()).append(':').append(property.getType()).append('=');
            builder.append(property.getType().equals("byte")
                ? String.valueOf(((Byte) property.getValue()).intValue())
                : String.valueOf(property.getValue())).append(';');
        }
        return builder.toString();
    }

    private String canonicalReference(final JsonObject block) {
        final StringBuilder builder =
            new StringBuilder(block.get("name").getAsString()).append('|');
        final JsonArray states = block.getAsJsonArray("states");
        final List<JsonObject> sorted = new ArrayList<>();
        states.forEach(element -> sorted.add(element.getAsJsonObject()));
        sorted.sort(Comparator.comparing(a -> a.get("name").getAsString()));
        for (final JsonObject state : sorted) {
            final String type = state.get("type").getAsString();
            builder.append(state.get("name").getAsString()).append(':').append(type).append('=');
            builder.append(state.get("value").getAsString()).append(';');
        }
        return builder.toString();
    }

    private static void log(final boolean verbose, final String message) {
        if (verbose) {
            System.out.println(message);
        }
    }

    private static void sleep(final long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}