package dev.kaooot.debugger.util.protocoldocs.model.property;

import dev.kaooot.debugger.util.protocoldocs.model.BedrockType;
import dev.kaooot.debugger.util.protocoldocs.model.ValueType;
import com.google.gson.annotations.SerializedName;
import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Node;
import java.util.Comparator;
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
public class BedrockCondition extends BedrockProperty {

    private List<BedrockProperty> oneOf;
    @SerializedName("x-control-value-type")
    private ValueType controlValueType;

    @Override
    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<String, BedrockType> definitions, List<Integer> lastOnes,
                       Attributes<? extends ForNode> style) {
        Node node = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", label);
        if (style != null) {
            node = node.with(style);
        }
        id.incrementAndGet();
        final Node controlValueNode = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", "Control Value Type");
        id.incrementAndGet();
        final String controlValueTypeLabel = this.getTypeName(
            false,
            true,
            this.controlValueType
        );
        final Node controlValueTypeNode = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", controlValueTypeLabel);
        lastOnes.add(id.get());
        id.incrementAndGet();
        Node dependencyNode = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", "Dependency on 'Control Value Type'")
            .with(Shape.NOTE);
        final List<BedrockProperty> sortedOneOf = this.oneOf.stream()
            .sorted(Comparator.comparingInt(BedrockProperty::getOrdinalIndex))
            .toList();
        for (final BedrockProperty property : sortedOneOf) {
            id.incrementAndGet();
            final Node propertyNode = property.toNode(
                id,
                String.valueOf(property.getOrdinalIndex()),
                false,
                definitions,
                lastOnes,
                Shape.DIAMOND
            );
            dependencyNode = dependencyNode.link(propertyNode);
            lastOnes.add(id.get());
        }
        return node.link(controlValueNode.link(controlValueTypeNode)).link(dependencyNode);
    }

    @Override
    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<String, BedrockType> definitions, List<Integer> lastOnes) {
        return this.toNode(id, label, optional, definitions, lastOnes, null);
    }
}