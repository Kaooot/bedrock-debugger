package dev.kaooot.debugger.core.blockpalette;

import dev.kaooot.debugger.core.memory.ProcessMemory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public final class BlockStateReader {

    private enum TagType {
        BYTE,
        INT,
        STRING,
        COMPOUND
    }

    @Value
    public static class StateProperty {
        String name;
        String type;
        Object value;
    }

    @Value
    public static class State {
        List<StateProperty> properties;
        int version;
    }

    private final ProcessMemory process;
    private final Layout layout;
    private final Map<Long, TagType> tags = new HashMap<>();
    private final byte[] node = new byte[32];
    private final byte[] text = new byte[256];

    public BlockStateReader(final ProcessMemory process, final Layout layout,
                            final Iterable<Long> sampleStates) {
        this.process = process;
        this.layout = layout;
        for (final long state : sampleStates) {
            this.learn(state);
            if (this.isReady()) {
                break;
            }
        }
    }

    public boolean isReady() {
        return new HashSet<>(this.tags.values()).size() >= 4;
    }

    public State read(final long state) {
        final List<StateProperty> properties = new ArrayList<>();
        int version = 0;
        final long head = this.process.readPointer(state + this.layout.blockStateNbt);
        if (head < ProcessMemory.MIN_POINTER) {
            return new State(properties, 0);
        }
        for (final long entry : this.nodes(head)) {
            final String key =
                Layout.readStdString(this.process, entry + Layout.MAP_NODE_KEY, this.text);
            if ("version".equals(key)) {
                if (this.value(entry) instanceof Integer v) {
                    version = v;
                }
            } else if ("states".equals(key)) {
                if (this.value(entry) instanceof Long inner && inner >= ProcessMemory.MIN_POINTER) {
                    for (final long stateNode : this.nodes(inner)) {
                        final String name =
                            Layout.readStdString(this.process, stateNode + Layout.MAP_NODE_KEY,
                                this.text);
                        final Object value = this.value(stateNode);
                        if (name != null && value != null) {
                            properties.add(new StateProperty(name, typeOf(value), value));
                        }
                    }
                }
            }
        }
        properties.sort(Comparator.comparing(StateProperty::getName));
        return new State(properties, version);
    }

    private void learn(final long state) {
        final long head = this.process.readPointer(state + this.layout.blockStateNbt);
        if (head < ProcessMemory.MIN_POINTER) {
            return;
        }
        for (final long entry : this.nodes(head)) {
            final long vtable = this.process.readPointer(entry + Layout.MAP_NODE_VTABLE);
            if (vtable < ProcessMemory.MIN_POINTER) {
                continue;
            }
            switch (String.valueOf(
                Layout.readStdString(this.process, entry + Layout.MAP_NODE_KEY, this.text))) {
                case "name" -> this.tags.put(vtable, TagType.STRING);
                case "version" -> this.tags.put(vtable, TagType.INT);
                case "states" -> this.tags.put(vtable, TagType.COMPOUND);
                default -> {
                }
            }
        }
        for (final long entry : this.nodes(head)) {
            if (!"states".equals(
                Layout.readStdString(this.process, entry + Layout.MAP_NODE_KEY, this.text))) {
                continue;
            }
            if (!(this.value(entry) instanceof Long inner) || inner < ProcessMemory.MIN_POINTER) {
                continue;
            }
            for (final long stateNode : this.nodes(inner)) {
                final long vtable = this.process.readPointer(stateNode + Layout.MAP_NODE_VTABLE);
                if (vtable >= ProcessMemory.MIN_POINTER && !this.tags.containsKey(vtable)) {
                    this.tags.put(vtable, TagType.BYTE);
                }
            }
        }
    }

    private Object value(final long entry) {
        final long vtable = this.process.readPointer(entry + Layout.MAP_NODE_VTABLE);
        final TagType kind = this.tags.get(vtable);
        if (kind == null) {
            return null;
        }
        final long payload = entry + Layout.MAP_NODE_PAYLOAD;
        return switch (kind) {
            case BYTE -> {
                final byte[] one = new byte[1];
                yield this.process.tryRead(payload, one, 1) ? one[0] : null;
            }
            case INT -> {
                final byte[] four = new byte[4];
                yield this.process.tryRead(payload, four, 4) ? ProcessMemory.getInt(four, 0) : null;
            }
            case STRING -> Layout.readStdString(this.process, payload, this.text);
            case COMPOUND -> this.process.readPointer(payload);
        };
    }

    private List<Long> nodes(final long head) {
        final List<Long> found = new ArrayList<>();
        final Set<Long> seen = new HashSet<>();
        final Deque<Long> pending = new ArrayDeque<>();
        pending.push(this.process.readPointer(head + Layout.MAP_NODE_PARENT));
        for (int guard = 0; !pending.isEmpty() && guard < 8192; guard++) {
            final long current = pending.pop();
            if (current < ProcessMemory.MIN_POINTER || current == head || !seen.add(current)) {
                continue;
            }
            if (!this.process.tryRead(current, this.node, this.node.length)) {
                continue;
            }
            if (this.node[Layout.MAP_NODE_FLAGS + 1] == 1) {
                continue;
            }
            found.add(current);
            pending.push(ProcessMemory.getLong(this.node, Layout.MAP_NODE_LEFT));
            pending.push(ProcessMemory.getLong(this.node, Layout.MAP_NODE_RIGHT));
        }
        return found;
    }

    private static String typeOf(final Object value) {
        if (value instanceof Byte) {
            return "byte";
        }
        if (value instanceof Integer) {
            return "int";
        }
        return "string";
    }

    public static int deriveStateNbt(final ProcessMemory process, final Layout layout,
                                     final List<BlockLayoutDeriver.Block> blocks) {
        final int reach = 512;
        final byte[] text = new byte[256];
        final Map<Integer, Integer> counts = new HashMap<>();

        for (final BlockLayoutDeriver.Block block : blocks) {
            final long state = process.readPointer(
                block.getAddress() + layout.nameInsideLegacy + layout.defaultStatePointer);
            if (state < ProcessMemory.MIN_POINTER || !process.isMapped(state)) {
                continue;
            }
            for (int at = 0; at + 8 <= reach; at += 8) {
                final long map = process.readPointer(state + at);
                if (map < ProcessMemory.MIN_POINTER || !process.isMapped(map)) {
                    continue;
                }
                final long root = process.readPointer(map + Layout.MAP_NODE_PARENT);
                if (root < ProcessMemory.MIN_POINTER || !process.isMapped(root)) {
                    continue;
                }
                if (hasNameKey(process, map, root, text)) {
                    counts.merge(at, 1, Integer::sum);
                }
            }
        }
        return counts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(-1);
    }

    private static boolean hasNameKey(final ProcessMemory process, final long map, final long root,
                                      final byte[] text) {
        final byte[] node = new byte[32];
        final Set<Long> seen = new HashSet<>();
        final Deque<Long> pending = new ArrayDeque<>();
        pending.push(root);
        while (!pending.isEmpty() && seen.size() < 64) {
            final long entry = pending.pop();
            if (entry < ProcessMemory.MIN_POINTER || entry == map || !seen.add(entry) ||
                !process.isMapped(entry)) {
                continue;
            }
            if (!process.tryRead(entry, node, node.length)) {
                continue;
            }
            if (node[Layout.MAP_NODE_FLAGS + 1] == 0
                &&
                "name".equals(Layout.readStdString(process, entry + Layout.MAP_NODE_KEY, text))) {
                return true;
            }
            pending.push(ProcessMemory.getLong(node, Layout.MAP_NODE_LEFT));
            pending.push(ProcessMemory.getLong(node, Layout.MAP_NODE_RIGHT));
        }
        return false;
    }
}