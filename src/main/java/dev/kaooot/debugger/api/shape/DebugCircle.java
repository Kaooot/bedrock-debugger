package dev.kaooot.debugger.api.shape;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class DebugCircle extends DebugSphere {

    @Override
    public ScriptPrimitiveShapeType getType() {
        return ScriptPrimitiveShapeType.CIRCLE;
    }
}