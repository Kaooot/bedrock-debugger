package dev.kaooot.debugger.api.shape;

import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.LineDataPayload;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class DebugLine extends DebugShape<LineDataPayload> {

    private Vector3f endLocation;

    @Override
    public ScriptPrimitiveShapeType getType() {
        return ScriptPrimitiveShapeType.LINE;
    }

    @Override
    protected LineDataPayload renderExtra() {
        Objects.requireNonNull(this.endLocation, "The end location must not be null");
        final LineDataPayload payload = new LineDataPayload();
        payload.setLineEndLocation(this.endLocation);
        return payload;
    }
}