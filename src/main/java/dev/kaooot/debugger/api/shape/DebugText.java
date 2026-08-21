package dev.kaooot.debugger.api.shape;

import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.TextDataPayload;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class DebugText extends DebugShape<TextDataPayload> {

    private String text;
    private boolean useRotation;
    private Integer backgroundColor;
    private float lineGapHeight;
    private boolean depthTest;
    private boolean showBackface;
    private boolean showTextBackface;

    @Override
    public ScriptPrimitiveShapeType getType() {
        return ScriptPrimitiveShapeType.TEXT;
    }

    @Override
    protected TextDataPayload renderExtra() {
        Objects.requireNonNull(this.text, "The text must not be null");
        final TextDataPayload payload = new TextDataPayload();
        payload.setText(this.text);
        payload.setUseRotation(this.useRotation);
        payload.setBackgroundColor(this.backgroundColor);
        payload.setDepthTest(this.depthTest);
        payload.setShowBackface(this.showBackface);
        payload.setShowTextBackface(this.showTextBackface);
        return payload;
    }
}