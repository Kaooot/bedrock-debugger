package dev.kaooot.debugger.screen;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.Value;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketDefinition;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.network.NetworkConstants;
import dev.kaooot.debugger.util.DebugElement;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ClientNetworkStatsScreen implements DebugScreen {

    private final Object2IntMap<NetworkPacketInfo> map = new Object2IntOpenHashMap<>();

    private long totalSentCounter = 0L;
    private long totalReceivedCounter = 0L;

    @Override
    public void render(BedrockDebuggerProxy proxy) {
        proxy.getServer().sendDebugInfos(
            this.getElement(),
            this.getClientNetworkStats(proxy)
        );
    }

    @Override
    public int getIndex() {
        return 2;
    }

    @Override
    public DebugElement getElement() {
        return DebugElement.SMALL_INFO;
    }

    public void increasePacketCounter(BedrockPacket packet, boolean fromClient) {
        final BedrockPacketDefinition<?> definition =
            NetworkConstants.CODEC.getPacketDefinition(packet.getClass());
        final NetworkPacketInfo info = new NetworkPacketInfo(
            packet.getClass().getSimpleName(),
            packet.getPacketType(),
            definition == null ? -1 : definition.getId(),
            fromClient
        );
        this.map.put(info, this.map.getOrDefault(info, 0) + 1);
        if (fromClient) {
            this.totalSentCounter++;
        } else {
            this.totalReceivedCounter++;
        }
    }

    private List<String> getClientNetworkStats(BedrockDebuggerProxy proxy) {
        final List<String> list = new ObjectArrayList<>();
        list.add("§lClient Network Stats§r");
        list.add(" ");
        final List<Object2IntMap.Entry<NetworkPacketInfo>> sentInfos =
            this.map.object2IntEntrySet().stream()
                .filter(entry -> entry.getKey().isFromClient())
                .sorted((o1, o2) ->
                    String.CASE_INSENSITIVE_ORDER.compare(o1.getKey().getPacketName(),
                        o2.getKey().getPacketName())
                )
                .toList();
        final List<Object2IntMap.Entry<NetworkPacketInfo>> receivedInfos =
            this.map.object2IntEntrySet().stream()
                .filter(entry -> !entry.getKey().isFromClient())
                .sorted((o1, o2) ->
                    String.CASE_INSENSITIVE_ORDER.compare(o1.getKey().getPacketName(),
                        o2.getKey().getPacketName())
                )
                .toList();
        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);
        list.add("Total Sent: " + this.totalSentCounter);
        for (final Object2IntMap.Entry<NetworkPacketInfo> sentInfo : sentInfos) {
            this.addInfo(list, sentInfo, settingsConfig);
        }
        list.add(" ");
        list.add("Total Received: " + this.totalReceivedCounter);
        for (final Object2IntMap.Entry<NetworkPacketInfo> receivedInfo : receivedInfos) {
            this.addInfo(list, receivedInfo, settingsConfig);
        }
        return list;
    }

    @Value
    private static class NetworkPacketInfo {
        String packetName;
        BedrockPacketType packetType;
        int packetId;
        boolean fromClient;
    }

    private void addInfo(List<String> list, Object2IntMap.Entry<NetworkPacketInfo> entry,
                         SettingsConfig settingsConfig) {
        final NetworkPacketInfo info = entry.getKey();
        final String first = info.getPacketId() +
            " ".repeat(4 - String.valueOf(info.getPacketId()).length()) + info.getPacketName();
        final String last = "Num " + entry.getIntValue();
        if (entry.getIntValue() >= settingsConfig.getCnsScreenMinPacketNum()) {
            list.add(first + " (" + last + ")");
        }
    }
}