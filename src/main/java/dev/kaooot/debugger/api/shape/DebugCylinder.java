package dev.kaooot.debugger.api.shape;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.CylinderDataPayload;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DebugCylinder extends DebugShape<CylinderDataPayload> {

    private Vector2f radiusX;
    private Vector2f radiusZ;
    private float height;
    private int numSegments;

    @Override
    public ScriptPrimitiveShapeType getType() {
        return ScriptPrimitiveShapeType.CYLINDER;
    }

    @Override
    protected CylinderDataPayload renderExtra() {
        final CylinderDataPayload payload = new CylinderDataPayload();
        payload.setRadiusX(this.radiusX);
        payload.setRadiusZ(this.radiusZ);
        payload.setHeight(this.height);
        payload.setNumSegments(this.numSegments);
        return payload;
    }
}