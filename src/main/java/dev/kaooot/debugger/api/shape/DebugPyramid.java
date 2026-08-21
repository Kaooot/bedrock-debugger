package dev.kaooot.debugger.api.shape;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.PyramidDataPayload;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DebugPyramid extends DebugShape<PyramidDataPayload> {

    private float width;
    private Float depth;
    private float height;

    @Override
    public ScriptPrimitiveShapeType getType() {
        return ScriptPrimitiveShapeType.PYRAMID;
    }

    @Override
    protected PyramidDataPayload renderExtra() {
        final PyramidDataPayload payload = new PyramidDataPayload();
        payload.setWidth(this.width);
        payload.setDepth(this.depth);
        payload.setHeight(this.height);
        return payload;
    }
}