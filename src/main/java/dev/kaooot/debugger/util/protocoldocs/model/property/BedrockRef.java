package dev.kaooot.debugger.util.protocoldocs.model.property;

import dev.kaooot.debugger.util.protocoldocs.model.BedrockType;
import com.google.gson.annotations.SerializedName;
import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Node;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BedrockRef extends BedrockProperty {

    @SerializedName("$ref")
    private final String ref;
    private BedrockType parent;

    @Override
    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<Long, BedrockType> definitions, List<Integer> lastOnes,
                       Attributes<? extends ForNode> style) {
        this.setParent(
            definitions == null ? null : definitions.getOrDefault(this.getRefId(), null)
        );
        Node node = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", label);
        if (style != null) {
            node = node.with(style);
        }
        id.incrementAndGet();
        if (this.parent == null) {
            node = node.link(
                Factory.node(String.valueOf(id.get()))
                    .with("id", id)
                    .with("label", "null")
            );
            lastOnes.add(id.get());
            return node;
        }
        Node typeDefinition = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", this.parent.getTitle());
        while (this.parent != null && this.parent.getProperties() == null &&
            this.parent.getRef() != null){
            this.parent = definitions.get(this.parent.getRefId());
        }
        final Map<String, BedrockProperty> properties = this.parent.getProperties();
        if (properties != null) {
            final List<Map.Entry<String, BedrockProperty>> sortedProps = properties.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(o -> o.getValue().getOrdinalIndex()))
                .toList();
            for (final Map.Entry<String, BedrockProperty> entry : sortedProps) {
                id.incrementAndGet();
                final String name = entry.getKey();
                final Node link = entry.getValue().toNode(
                    id,
                    name,
                    !this.parent.getRequired().contains(name),
                    definitions,
                    lastOnes
                );
                typeDefinition = typeDefinition.link(link);
                lastOnes.add(id.get());
            }
        } else {
            id.incrementAndGet();
            typeDefinition = typeDefinition.link(
                Factory.node(String.valueOf(id.get()))
                    .with("id", id)
                    .with("label", this.parent.getType())
            );
        }
        lastOnes.add(id.get());
        node = node.link(typeDefinition);
        return node;
    }

    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<Long, BedrockType> definitions, List<Integer> lastOnes) {
        return this.toNode(id, label, optional, definitions, lastOnes, null);
    }

    private long getRefId() {
        return Long.parseLong(this.ref.split("#/definitions/")[1]);
    }
}