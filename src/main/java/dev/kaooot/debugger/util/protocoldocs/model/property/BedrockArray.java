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

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BedrockArray extends BedrockProperty {

    private BedrockProperty items;
    private Integer maxItems;

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
        final Node listSizeNode = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", "List Size");
        id.incrementAndGet();
        final ValueType type = this.getUnderlyingType() == null ?
            ValueType.UNSIGNED_INT : this.getUnderlyingType();
        String listSizeTypeLabel = this.getTypeName(
            false,
            true,
            type
        );
        if (this.getSerializationOptions() != null && this.getSerializationOptions()
            .contains(SerializationOption.NO_SIZE_COMPRESSION)) {
            listSizeTypeLabel = this.getTypeName(
                false,
                false,
                type
            );
        }
        final Node listSizeTypeNode = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", listSizeTypeLabel);
        lastOnes.add(id.get());
        id.incrementAndGet();
        Node exampleElementValueNode = this.items.toNode(
            id,
            "example element",
            false,
            definitions,
            lastOnes,
            Style.DOTTED
        );
        node = node.link(
            listSizeNode.link(listSizeTypeNode)
        ).link(
            exampleElementValueNode
        );
        lastOnes.add(id.get());
        return node;
    }

    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<Long, BedrockType> definitions, List<Integer> lastOnes) {
        return this.toNode(id, label, optional, definitions, lastOnes, null);
    }
}