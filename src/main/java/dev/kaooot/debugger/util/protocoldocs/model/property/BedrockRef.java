package dev.kaooot.debugger.util.protocoldocs.model.property;

import dev.kaooot.debugger.util.protocoldocs.model.BedrockType;
import com.google.gson.annotations.SerializedName;
import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Node;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private static final ThreadLocal<Set<String>> EXPANDING =
        ThreadLocal.withInitial(HashSet::new);

    @SerializedName("$ref")
    private final String ref;
    private BedrockType parent;

    @Override
    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<String, BedrockType> definitions, List<Integer> lastOnes,
                       Attributes<? extends ForNode> style) {
        final String refKey = this.getRefKey();
        this.setParent(definitions == null ? null : definitions.get(refKey));
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
        final Set<String> expanding = EXPANDING.get();
        if (!expanding.add(refKey)) {
            node = node.link(
                Factory.node(String.valueOf(id.get()))
                    .with("id", id)
                    .with("label", this.parent.getTitle())
            );
            lastOnes.add(id.get());
            return node;
        }
        try {
            Node typeDefinition = Factory.node(String.valueOf(id.get()))
                .with("id", id)
                .with("label", this.parent.getTitle());
            int guard = 0;
            while (this.parent != null && this.parent.getProperties() == null &&
                this.parent.getRef() != null && guard++ < 64) {
                this.parent = definitions.get(this.parent.getRefKey());
            }
            final Map<String, BedrockProperty> properties =
                this.parent == null ? null : this.parent.getProperties();
            if (properties != null) {
                final Set<String> required = this.parent.getRequired();
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
                        required != null && !required.contains(name),
                        definitions,
                        lastOnes
                    );
                    typeDefinition = typeDefinition.link(link);
                    lastOnes.add(id.get());
                }
            } else {
                id.incrementAndGet();
                final String leafLabel;
                if (this.parent == null) {
                    leafLabel = "null";
                } else if (this.parent.isEnum()) {
                    leafLabel = "enum " + this.parent.getTitle();
                } else {
                    leafLabel = String.valueOf(this.parent.getType());
                }
                typeDefinition = typeDefinition.link(
                    Factory.node(String.valueOf(id.get()))
                        .with("id", id)
                        .with("label", leafLabel)
                );
            }
            lastOnes.add(id.get());
            node = node.link(typeDefinition);
            return node;
        } finally {
            expanding.remove(refKey);
        }
    }

    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<String, BedrockType> definitions, List<Integer> lastOnes) {
        return this.toNode(id, label, optional, definitions, lastOnes, null);
    }

    private String getRefKey() {
        final int slash = this.ref.lastIndexOf('/');
        return slash >= 0 ? this.ref.substring(slash + 1) : this.ref;
    }
}