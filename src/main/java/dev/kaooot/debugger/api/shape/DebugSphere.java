package dev.kaooot.debugger.api.shape;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.SphereDataPayload;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class DebugSphere extends DebugShape<SphereDataPayload> {

    private Integer numSegments = 0;

    @Override
    public ScriptPrimitiveShapeType getType() {
        return ScriptPrimitiveShapeType.SPHERE;
    }

    @Override
    protected SphereDataPayload renderExtra() {
        final SphereDataPayload payload = new SphereDataPayload();
        payload.setNumSegments(this.numSegments);
        return payload;
    }
}