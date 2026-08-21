package dev.kaooot.debugger.api.shape;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.Dimension;
import org.cloudburstmc.protocol.bedrock.data.payload.common.DimensionType;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ExtraShapeDataPayload;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.PrimitiveShapeDataPayload;
import org.cloudburstmc.protocol.bedrock.data.payload.shape.ScriptPrimitiveShapeType;
import org.cloudburstmc.protocol.common.util.Preconditions;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
public abstract class DebugShape<T extends ExtraShapeDataPayload> {

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    protected long networkId = -1L;

    protected String id;
    protected Vector3f location;
    protected Float scale = 1f;
    protected Vector3f rotation;
    protected Float totalTimeLeft;
    protected Integer color;
    protected DimensionType dimension = DimensionType.from(Dimension.OVERWORLD);
    protected Long attachedToEntityID;

    public abstract ScriptPrimitiveShapeType getType();

    public PrimitiveShapeDataPayload render() {
        Preconditions.checkArgument(this.networkId != -1L, "The network id is invalid");
        Objects.requireNonNull(this.location, "The location must not be null");
        Objects.requireNonNull(this.dimension, "The dimension must not be null");
        final PrimitiveShapeDataPayload shapeData = new PrimitiveShapeDataPayload();
        shapeData.setNetworkId(this.networkId);
        shapeData.setShapeType(this.getType());
        shapeData.setLocation(this.location);
        shapeData.setScale(this.scale);
        shapeData.setRotation(this.rotation);
        shapeData.setTotalTimeLeft(this.totalTimeLeft);
        shapeData.setColor(this.color);
        shapeData.setDimension(this.dimension);
        shapeData.setAttachedToEntityID(this.attachedToEntityID);
        shapeData.setExtraShapeData(this.renderExtra());
        return shapeData;
    }

    protected abstract T renderExtra();
}