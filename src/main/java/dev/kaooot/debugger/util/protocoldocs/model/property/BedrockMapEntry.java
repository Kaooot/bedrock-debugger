package dev.kaooot.debugger.util.protocoldocs.model.property;

import dev.kaooot.debugger.util.protocoldocs.model.BedrockType;
import dev.kaooot.debugger.util.protocoldocs.model.SerializationOption;
import dev.kaooot.debugger.util.protocoldocs.model.ValueType;
import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Node;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BedrockMapEntry extends BedrockProperty {

    private Properties properties;

    @Value
    public static class Properties {
        BedrockProperty key;
        BedrockProperty value;
    }

    @Override
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
        final Node mapSizeNode = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", "Map Size");
        id.incrementAndGet();
        final ValueType type = this.getUnderlyingType() == null ?
            ValueType.UNSIGNED_INT : this.getUnderlyingType();
        String mapSizeTypeLabel = this.getTypeName(
            false,
            true,
            type
        );
        if (this.getSerializationOptions() != null && this.getSerializationOptions()
            .contains(SerializationOption.NO_SIZE_COMPRESSION)) {
            mapSizeTypeLabel = this.getTypeName(
                false,
                false,
                type
            );
        }
        final Node mapSizeTypeNode = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", mapSizeTypeLabel);
        lastOnes.add(id.get());
        id.incrementAndGet();
        Node exampleElementValueNode = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", "example element")
            .with(Style.DOTTED);
        id.incrementAndGet();
        final Node keyNode = this.properties.getKey().toNode(
            id,
            "key",
            false,
            definitions,
            lastOnes
        );
        lastOnes.add(id.get());
        id.incrementAndGet();
        final Node valueNode = this.properties.getValue().toNode(
            id,
            "value",
            false,
            definitions,
            lastOnes
        );
        lastOnes.add(id.get());
        exampleElementValueNode = exampleElementValueNode.link(keyNode).link(valueNode);
        return node.link(mapSizeNode.link(mapSizeTypeNode)).link(exampleElementValueNode);
    }

    @Override
    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<Long, BedrockType> definitions, List<Integer> lastOnes) {
        return this.toNode(id, label, optional, definitions, lastOnes, null);
    }
}