package dev.kaooot.debugger.util.protocoldocs.model.property;

import dev.kaooot.debugger.util.protocoldocs.model.BedrockType;
import com.google.gson.annotations.SerializedName;
import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForNode;
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
public class BedrockMap extends BedrockProperty {

    private BedrockProperty additionalProperties;
    @SerializedName("default")
    private Object defaultValue;
    private Integer maxProperties;

    @Override
    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<Long, BedrockType> definitions, List<Integer> lastOnes,
                       Attributes<? extends ForNode> style) {
        return this.additionalProperties.toNode(id, label, optional, definitions, lastOnes, style);
    }

    @Override
    public Node toNode(AtomicInteger id, String label, boolean optional,
                       Map<Long, BedrockType> definitions, List<Integer> lastOnes) {
        return this.additionalProperties.toNode(id, label, optional, definitions, lastOnes);
    }
}