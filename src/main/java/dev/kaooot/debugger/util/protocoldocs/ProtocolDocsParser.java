package dev.kaooot.debugger.util.protocoldocs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import dev.kaooot.debugger.util.protocoldocs.model.BedrockType;
import dev.kaooot.debugger.util.protocoldocs.model.deserializer.PropertyDeserializer;
import dev.kaooot.debugger.util.protocoldocs.model.property.BedrockProperty;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class ProtocolDocsParser {

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(BedrockProperty.class, new PropertyDeserializer())
        .setPrettyPrinting()
        .create();

    public File parse(File dataLogsFolder, Path path) {
        final Map<String, BedrockType> definitions = this.readSchemas(path);
        final File dataFolder = new File("data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File protocolDocsDataFolder = null;
        for (final BedrockType schema : definitions.values()) {
            if (schema.isEnum()) {
                final File folder = this.writeEnumMarkdown(dataLogsFolder, schema);
                if (folder != null) {
                    protocolDocsDataFolder = folder;
                }
                continue;
            }
            if (!schema.isPacket()) {
                continue;
            }
            final BedrockType payload = this.resolve(schema, definitions);
            if (payload == null) {
                continue;
            }
            final String title = schema.getTitle();
            final Map<String, BedrockProperty> properties = payload.getProperties();
            if (properties == null) {
                continue;
            }
            final Set<String> required = payload.getRequired();

            final AtomicInteger idCounter = new AtomicInteger();
            Node mainNode = Factory.node("")
                .with("id", idCounter.get())
                .with("label", title);
            idCounter.incrementAndGet();
            final List<Integer> lastOnes = new IntArrayList();

            final List<Map.Entry<String, BedrockProperty>> sortedProps = properties.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(o -> o.getValue().getOrdinalIndex()))
                .toList();
            for (final Map.Entry<String, BedrockProperty> entry : sortedProps) {
                final String name = entry.getKey();
                final BedrockProperty property = entry.getValue();
                final Node node = property.toNode(
                    idCounter,
                    name,
                    required != null && !required.contains(name),
                    definitions,
                    lastOnes
                );
                mainNode = mainNode.link(node);
                idCounter.incrementAndGet();
            }

            final List<Node> subGraphNodes = new ObjectArrayList<>();
            for (final int id : lastOnes) {
                subGraphNodes.add(Factory.node(String.valueOf(id)));
            }

            final Graph subGraph = Factory.graph("subGraph")
                .graphAttr()
                .with("rank", "same")
                .with(subGraphNodes);
            final Graph graph = Factory.graph(title)
                .directed()
                .graphAttr()
                .with(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT))
                .with(mainNode).with(subGraph);

            try {
                protocolDocsDataFolder = new File(
                    dataLogsFolder + "/protocoldocs-v" + schema.getProtocolVersion()
                );
                if (!protocolDocsDataFolder.exists()) {
                    protocolDocsDataFolder.mkdirs();
                }
                Graphviz.fromGraph(graph)
                    .render(Format.SVG)
                    .toFile(
                        new File(protocolDocsDataFolder, title + ".svg")
                    );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return protocolDocsDataFolder;
    }

    private File writeEnumMarkdown(File dataLogsFolder, BedrockType schema) {
        final List<String> values = schema.getEnumValues();
        final List<Long> binaryValues = schema.getEnumBinaryValues();
        if (values == null || values.isEmpty() || binaryValues == null ||
            binaryValues.isEmpty()) {
            return null;
        }
        final String title = schema.getTitle();
        final File protocolDocsDataFolder = new File(
            dataLogsFolder + "/protocoldocs-v" + schema.getProtocolVersion()
        );
        final File enumsFolder = new File(protocolDocsDataFolder, "enums");
        if (!enumsFolder.exists()) {
            enumsFolder.mkdirs();
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("# ").append(title).append("\n\n");
        builder.append("| Name | ID |\n");
        builder.append("| --- | --- |\n");
        for (int i = 0; i < values.size(); i++) {
            final Long binaryValue = binaryValues != null && i < binaryValues.size()
                ? binaryValues.get(i)
                : null;
            builder.append("| ")
                .append(values.get(i))
                .append(" | ")
                .append(binaryValue != null ? binaryValue : "")
                .append(" |\n");
        }

        final String finalTitle = title.replace(":", "_");
        try (final FileWriter writer = new FileWriter(new File(enumsFolder, finalTitle + ".md"))) {
            writer.write(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return protocolDocsDataFolder;
    }

    private BedrockType resolve(BedrockType type, Map<String, BedrockType> definitions) {
        BedrockType current = type;
        int guard = 0;
        while (current != null && current.getProperties() == null && current.getRef() != null &&
            guard++ < 64) {
            current = definitions.get(current.getRefKey());
        }
        return current;
    }

    private Map<String, BedrockType> readSchemas(Path folderPath) {
        final File file = folderPath.toFile();
        final Map<String, BedrockType> schemas = new Object2ObjectOpenHashMap<>();
        for (final File listFile : Objects.requireNonNull(file.listFiles())) {
            final String fileName = listFile.getName();
            if (fileName.startsWith(".") || !fileName.endsWith(".json")) {
                continue;
            }
            try (final FileReader reader = new FileReader(listFile)) {
                schemas.put(fileName, this.gson.fromJson(reader, BedrockType.class));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read the protocol schema", e);
            }
        }
        return schemas;
    }
}