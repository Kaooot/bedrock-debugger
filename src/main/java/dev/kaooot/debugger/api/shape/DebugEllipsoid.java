package dev.kaooot.debugger.api.shape;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.EllipsoidDataPayload;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DebugEllipsoid extends DebugShape<EllipsoidDataPayload> {

    private Vector3f radii;
    private int segmentsPerAxis;

    @Override
    public ScriptPrimitiveShapeType getType() {
        return ScriptPrimitiveShapeType.ELLIPSOID;
    }

    @Override
    protected EllipsoidDataPayload renderExtra() {
        final EllipsoidDataPayload payload = new EllipsoidDataPayload();
        payload.setRadii(this.radii);
        payload.setSegmentsPerAxis(this.segmentsPerAxis);
        return payload;
    }
}