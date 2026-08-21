package dev.kaooot.debugger.api.shape;

import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ArrowDataPayload;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class DebugArrow extends DebugShape<ArrowDataPayload> {

    private Vector3f endLocation;
    private Float headLength;
    private Float headRadius;
    private Integer headSegments;

    @Override
    public ScriptPrimitiveShapeType getType() {
        return ScriptPrimitiveShapeType.ARROW;
    }

    @Override
    protected ArrowDataPayload renderExtra() {
        Objects.requireNonNull(this.endLocation, "The end location must not be null");
        final ArrowDataPayload payload = new ArrowDataPayload();
        payload.setArrowEndLocation(this.endLocation);
        payload.setArrowHeadLength(this.headLength);
        payload.setArrowHeadRadius(this.headRadius);
        payload.setNumSegments(this.headSegments);
        return payload;
    }
}