package dev.kaooot.debugger.util.protocoldocs.model.property;

import dev.kaooot.debugger.util.protocoldocs.model.BedrockType;
import dev.kaooot.debugger.util.protocoldocs.model.SerializationOption;
import com.google.gson.annotations.SerializedName;
import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Node;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class BedrockEnum extends BedrockProperty {

    String title;
    @SerializedName("enum")
    List<String> values;

    @Override
    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<String, BedrockType> definitions, List<Integer> lastOnes,
                       Attributes<? extends ForNode> style) {
        final Node node = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", label);
        id.incrementAndGet();
        String typeName = this.getType().toString();
        if (this.getSerializationOptions() != null &&
            this.getSerializationOptions().contains(SerializationOption.ENUM_AS_VALUE)) {
            typeName = this.getTypeName(
                false,
                this.getSerializationOptions().contains(SerializationOption.COMPRESSION),
                this.getUnderlyingType()
            );
        }
        final Node enumTitleNode = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", this.getPrettifiedTitle());
        id.incrementAndGet();
        final Node typeNode = Factory.node(String.valueOf(id.get()))
            .with("id", id)
            .with("label", typeName);
        lastOnes.add(id.get());
        return node.link(enumTitleNode.link(typeNode));
    }

    @Override
    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<String, BedrockType> definitions, List<Integer> lastOnes) {
        return this.toNode(id, label, optional, definitions, lastOnes, null);
    }

    private String getPrettifiedTitle() {
        if (this.title == null || this.title.isEmpty()) {
            return "enum";
        }
        return this.title.startsWith("enum") ? this.title : "enum " + this.title;
    }
}