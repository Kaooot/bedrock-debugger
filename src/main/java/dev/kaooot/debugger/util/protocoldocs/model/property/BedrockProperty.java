package dev.kaooot.debugger.util.protocoldocs.model.property;

import dev.kaooot.debugger.util.protocoldocs.model.BedrockType;
import dev.kaooot.debugger.util.protocoldocs.model.SerializationOption;
import dev.kaooot.debugger.util.protocoldocs.model.Type;
import dev.kaooot.debugger.util.protocoldocs.model.ValueType;
import com.google.gson.annotations.SerializedName;
import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Node;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
public class BedrockProperty {

    private String description;
    private Type type;
    @SerializedName("x-underlying-type")
    private ValueType underlyingType;
    @SerializedName("x-serialization-options")
    private Set<SerializationOption> serializationOptions;
    @SerializedName("x-ordinal-index")
    private int ordinalIndex;
    private Float minimum;
    private Float maximum;

    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<Long, BedrockType> definitions, List<Integer> lastOnes,
                       Attributes<? extends ForNode> style) {
        Node node = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", label);
        if (style != null) {
            node = node.with(style);
        }
        id.incrementAndGet();
        String typeName = null;
        if (this.underlyingType != null) {
            final boolean compression = this.serializationOptions != null &&
                this.serializationOptions.contains(SerializationOption.COMPRESSION);
            typeName = this.getTypeName(optional, compression);
        } else if (this.type != null) {
            typeName = this.type.toString();
        } else if (this.description != null) {
            typeName = this.description;
        }
        final Node link = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", typeName);
        node = node.link(link);
        lastOnes.add(id.get());
        return node;
    }

    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<Long, BedrockType> definitions, List<Integer> lastOnes) {
        return this.toNode(id, label, optional, definitions, lastOnes, null);
    }

    protected String getTypeName(boolean optional, boolean compression) {
        return this.getTypeName(optional, compression, this.underlyingType);
    }

    protected String getTypeName(boolean optional, boolean compression, ValueType valueType) {
        String value = valueType.getId();
        if (compression) {
            switch (valueType) {
                case INT -> value = "varint";
                case UNSIGNED_INT -> value = "unsigned varint";
                case LONG -> value = "varint64";
                case UNSIGNED_LONG -> value = "unsigned varint64";
            }
        }
        return optional ? "std::optional<" + value + ">" : value;
    }
}