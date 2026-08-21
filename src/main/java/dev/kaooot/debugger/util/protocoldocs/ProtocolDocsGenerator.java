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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import dev.kaooot.debugger.util.protocoldocs.model.PacketProtocolSchema;
import dev.kaooot.debugger.util.protocoldocs.model.deserializer.PropertyDeserializer;
import dev.kaooot.debugger.util.protocoldocs.model.property.BedrockProperty;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class ProtocolDocsGenerator {

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(BedrockProperty.class, new PropertyDeserializer())
        .setPrettyPrinting()
        .create();

    public File generate(File dataLogsFolder, Path path) {
        final List<PacketProtocolSchema> schemas = this.readSchemas(path);
        final File dataFolder = new File("data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File protocolDocsDataFolder = null;
        for (final PacketProtocolSchema schema : schemas) {
            final String title = schema.getTitle();
            if (title.startsWith("enum")) {
                continue;
            }
            final AtomicInteger idCounter = new AtomicInteger();
            Node mainNode = Factory.node("")
                .with("id", idCounter.get())
                .with("label", title);
            idCounter.incrementAndGet();
            final List<Integer> lastOnes = new IntArrayList();
            final Map<String, BedrockProperty> properties = schema.getProperties();
            if (properties != null) {
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
                        schema.getRequired() != null && !schema.getRequired().contains(name),
                        schema.getDefinitions(),
                        lastOnes
                    );
                    mainNode = mainNode.link(
                        node
                    );
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
        }
        return protocolDocsDataFolder;
    }

    private List<PacketProtocolSchema> readSchemas(Path folderPath) {
        final File file = folderPath.toFile();
        final List<PacketProtocolSchema> schemas = new ObjectArrayList<>();
        for (final File listFile : Objects.requireNonNull(file.listFiles())) {
            if (listFile.getName().startsWith(".") ||
                listFile.getName().startsWith("_")) {
                continue;
            }
            try (final FileReader reader = new FileReader(listFile)) {
                schemas.add(this.gson.fromJson(reader, PacketProtocolSchema.class));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read the protocol schema", e);
            }
        }
        return schemas;
    }
}