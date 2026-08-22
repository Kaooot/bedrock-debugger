package dev.kaooot.debugger.api.shape;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.PrimitiveShapeDataPayload;
import org.cloudburstmc.protocol.bedrock.packet.PrimitiveShapesPacket;
import org.cloudburstmc.protocol.common.util.Preconditions;
import dev.kaooot.debugger.server.ProxiedServer;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class DebugShapeRenderer {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private final ProxiedServer server;
    private final List<DebugShape> shapes = Collections.synchronizedList(new ObjectArrayList<>());
    private final List<Long> usedNetworkIds = Collections.synchronizedList(new LongArrayList());

    /**
     * Renders a single debug shape on the client
     *
     * @param shape which should be rendered
     */
    public void renderShape(DebugShape shape) {
        Objects.requireNonNull(shape.getId(), "The shape identifier must not be null");
        Preconditions.checkArgument(!shape.getId().isEmpty(),
            "The shape identifier must not be empty");

        shape.setDimension(this.server.getProxy().getPlayer().getDimension());

        if (this.shapes.stream().noneMatch(s -> s.getId().equalsIgnoreCase(shape.getId()))) {
            shape.setNetworkId(this.generateNetworkId());
            this.shapes.add(shape);
        } else {
            final DebugShape existing = this.shapes.stream()
                .filter(value -> value.getId().equalsIgnoreCase(shape.getId()))
                .findFirst()
                .orElse(null);
            if (existing == null) {
                throw new NullPointerException("The debug shape could not be added because " +
                    "the existing debug shape is null");
            }
            shape.setNetworkId(existing.getNetworkId());
        }

        final PrimitiveShapesPacket packet = new PrimitiveShapesPacket();
        packet.getShapes().add(shape.render());

        this.server.sendPacket(packet);
    }

    /**
     * Renders several shapes on the client
     *
     * @param shapes which should be rendered
     */
    public void renderShapes(DebugShape... shapes) {
        final PrimitiveShapesPacket packet = new PrimitiveShapesPacket();

        for (final DebugShape shape : shapes) {
            Objects.requireNonNull(shape.getId(), "The shape identifier must not be null");
            Preconditions.checkArgument(!shape.getId().isEmpty(),
                "The shape identifier must not be empty");

            if (this.shapes.stream().noneMatch(s -> s.getId().equalsIgnoreCase(shape.getId()))) {
                shape.setNetworkId(this.generateNetworkId());
                this.shapes.add(shape);
            } else {
                final DebugShape existing = this.shapes.stream()
                    .filter(value -> value.getId().equalsIgnoreCase(shape.getId()))
                    .findFirst()
                    .orElse(null);
                if (existing == null) {
                    throw new NullPointerException("The debug shape could not be added because " +
                        "the existing debug shape is null");
                }
                shape.setNetworkId(existing.getNetworkId());
            }

            packet.getShapes().add(shape.render());
        }

        this.server.sendPacket(packet);
    }

    /**
     * Removes a rendered shape
     *
     * @param id the id of the shape
     */
    public void removeShape(String id, boolean clear) {
        final DebugShape shape = this.shapes.stream()
            .filter(debugShape -> debugShape.getId().equalsIgnoreCase(id))
            .findFirst()
            .orElse(null);

        if (shape != null) {
            final PrimitiveShapeDataPayload payload = new PrimitiveShapeDataPayload();
            payload.setNetworkId(shape.getNetworkId());

            this.sendPacket(Collections.singletonList(payload));

            if (clear) {
                this.shapes.removeIf(s -> s.getId().equalsIgnoreCase(shape.getId()));
                this.usedNetworkIds.remove(shape.getNetworkId());
            }
        } else {
            throw new NullPointerException("Tried to remove a debug shape that is null. id: " +
                id + ", clear: " + clear);
        }
    }

    public void removeShape(String id) {
        this.removeShape(id, false);
    }

    /**
     * Removes several rendered shapes
     *
     * @param ids the ids of the shapes
     */
    public void removeShapes(boolean clear, String... ids) {
        final List<PrimitiveShapeDataPayload> shapes = new ObjectArrayList<>();
        for (final String id : ids) {
            final DebugShape shape = this.shapes.stream()
                .filter(debugShape -> debugShape.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);

            if (shape == null) {
                continue;
            }

            final PrimitiveShapeDataPayload payload = new PrimitiveShapeDataPayload();
            payload.setNetworkId(shape.getNetworkId());

            shapes.add(payload);

            if (clear) {
                this.shapes.removeIf(s -> s.getId().equalsIgnoreCase(shape.getId()));
                this.usedNetworkIds.remove(shape.getNetworkId());
            }
        }

        if (shapes.isEmpty()) {
            throw new IllegalStateException("The shapes must not be empty. clear: " + clear + ", " +
                "ids: " + Arrays.toString(ids) + ", shapes: " +
                this.shapes.stream().map(DebugShape::getId).toList());
        }

        if (!clear) {
            this.sendPacket(shapes);
        } else {
            // race condition fix
            this.server.getEventLoop()
                .schedule(() -> this.sendPacket(shapes), 50, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Removes certain shapes selected by the given predicate
     *
     * @param predicate used to select the shapes which should be removed
     */
    public void clearShapes(Predicate<String> predicate) {
        final List<PrimitiveShapeDataPayload> shapes = new ObjectArrayList<>();
        for (final DebugShape shape : this.shapes) {
            final String id = shape.getId();
            if (!predicate.test(id)) {
                continue;
            }

            final PrimitiveShapeDataPayload payload = new PrimitiveShapeDataPayload();
            payload.setNetworkId(shape.getNetworkId());

            shapes.add(payload);
        }

        if (shapes.isEmpty()) {
            return;
        }

        this.shapes.removeIf(shape -> predicate.test(shape.getId()));

        for (final PrimitiveShapeDataPayload shape : shapes) {
            this.usedNetworkIds.remove(shape.getNetworkId());
        }

        // race condition fix
        this.server.getEventLoop()
            .schedule(() -> this.sendPacket(shapes), 50, TimeUnit.MILLISECONDS);
    }

    public void removeShapes(String... ids) {
        this.removeShapes(false, ids);
    }

    public boolean isShapeRendered(String id) {
        return this.shapes.stream().anyMatch(shape -> shape.getId().equalsIgnoreCase(id));
    }

    public <T extends DebugShape> T getShape(String id, Class<T> clazz) {
        final Object value = this.shapes.stream()
            .filter(shape -> shape.getId().equals(id) &&
                shape.getClass().equals(clazz))
            .findAny()
            .orElse(null);
        return value == null ? null : (T) value;
    }

    private long generateNetworkId() {
        long netId = RANDOM.nextLong();
        while (this.usedNetworkIds.contains(netId)) {
            netId = RANDOM.nextLong();
        }
        this.usedNetworkIds.add(netId);
        return netId;
    }

    private void sendPacket(List<PrimitiveShapeDataPayload> shapes) {
        final PrimitiveShapesPacket packet = new PrimitiveShapesPacket();
        packet.getShapes().addAll(shapes);
        this.server.sendPacket(packet);
    }
}