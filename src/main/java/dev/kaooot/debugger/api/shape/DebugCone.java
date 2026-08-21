package dev.kaooot.debugger.api.shape;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ConeDataPayload;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DebugCone extends DebugShape<ConeDataPayload> {

    private Vector2f radii;
    private float height;
    private int numSegments;

    @Override
    public ScriptPrimitiveShapeType getType() {
        return ScriptPrimitiveShapeType.CONE;
    }

    @Override
    protected ConeDataPayload renderExtra() {
        final ConeDataPayload payload = new ConeDataPayload();
        payload.setRadii(this.radii);
        payload.setHeight(this.height);
        payload.setNumSegments(this.numSegments);
        return payload;
    }
}