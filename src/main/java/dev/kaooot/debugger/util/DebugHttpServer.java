package dev.kaooot.debugger.util;

import com.sun.net.httpserver.HttpServer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import dev.kaooot.debugger.BedrockDebuggerProxy;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class DebugHttpServer {

    private static final InetSocketAddress ADDRESS = new InetSocketAddress(50005);

    private final BedrockDebuggerProxy proxy;

    private HttpServer server;

    private final Map<ListenerType, List<Consumer<byte[]>>> listeners =
        new Object2ObjectOpenHashMap<>();

    public DebugHttpServer(BedrockDebuggerProxy proxy) {
        this.proxy = proxy;
        try {
            this.server = HttpServer.create(ADDRESS, 0);
            this.server.createContext("/item_tags", exchange ->
                this.invokeListeners(ListenerType.ITEM_TAGS, exchange.getRequestBody()));
            this.server.createContext("/block_tags", exchange ->
                this.invokeListeners(ListenerType.BLOCK_TAGS, exchange.getRequestBody()));
            this.server.createContext("/block_states", exchange ->
                this.invokeListeners(ListenerType.BLOCKS, exchange.getRequestBody()));
            this.server.setExecutor(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        this.server.start();
        this.proxy.getLogger().debug("Debug Http server started on {}", ADDRESS);
    }

    public void stop() {
        this.server.stop(1);
    }

    public void addListener(ListenerType type, Consumer<byte[]> consumer) {
        this.listeners.computeIfAbsent(type, t -> new ObjectArrayList<>()).add(consumer);
    }

    private void invokeListeners(ListenerType type, InputStream requestBody) {
        try (final InputStream inputStream = requestBody) {
            if (!this.listeners.containsKey(type)) {
                return;
            }
            final byte[] data = inputStream.readAllBytes();
            for (final Consumer<byte[]> consumer : this.listeners.get(type)) {
                consumer.accept(data);
            }
            this.listeners.remove(type);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum ListenerType {
        ITEM_TAGS,
        BLOCK_TAGS,
        BLOCKS
    }
}