package dev.kaooot.debugger.api.shape;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.BoxDataPayload;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class DebugBox extends DebugShape<BoxDataPayload> {

    private Vector3f boxBound;

    @Override
    public ScriptPrimitiveShapeType getType() {
        return ScriptPrimitiveShapeType.BOX;
    }

    @Override
    protected BoxDataPayload renderExtra() {
        final BoxDataPayload payload = new BoxDataPayload();
        payload.setBoxBound(this.boxBound);
        return payload;
    }
}