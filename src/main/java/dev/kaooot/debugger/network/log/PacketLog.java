package dev.kaooot.debugger.network.log;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.network.NetworkConstants;
import imgui.ImGui;
import imgui.ImGuiListClipper;
import imgui.ImVec4;
import imgui.callback.ImListClipperCallback;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketDefinition;
import org.cloudburstmc.protocol.bedrock.data.PacketRecipient;
import org.cloudburstmc.protocol.bedrock.netty.BedrockPacketWrapper;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class PacketLog {

    private static final int MAX_ENTRIES = 8192;
    private static final int VIEW_HEX = 0;
    private static final int VIEW_BINARY = 1;
    private static final int VIEW_STRING = 2;
    private static final DateTimeFormatter TABLE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter FILE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS 'UTC'").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter FILE_NAME_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);
    private static final ImVec4 SERVERBOUND_COLOR = new ImVec4(0.45f, 0.85f, 0.45f, 1.0f);
    private static final ImVec4 CLIENTBOUND_COLOR = new ImVec4(0.40f, 0.70f, 1.0f, 1.0f);
    private static final ImVec4 BOTH_COLOR = new ImVec4(0.95f, 0.80f, 0.35f, 1.0f);
    private static final ImVec4 MUTED_COLOR = new ImVec4(0.6f, 0.6f, 0.6f, 1.0f);

    private final BedrockDebuggerProxy proxy;

    private final Object entriesLock = new Object();
    private final ArrayDeque<PacketLogEntry> entries = new ArrayDeque<>();
    private final AtomicLong sequence = new AtomicLong();
    private long changeCounter;

    private final Object fileLock = new Object();
    private Writer fileWriter;
    private String fileName = "";

    private volatile SettingsConfig settingsConfig;

    private final ImString filter = new ImString(100);
    private final ImBoolean paused = new ImBoolean(false);
    private final ImBoolean logToFile = new ImBoolean(false);
    private final ImBoolean captureToString = new ImBoolean(false);
    private final ImInt viewMode = new ImInt(VIEW_HEX);
    private final ImBoolean autoScroll = new ImBoolean(true);
    private long selectedSequence = -1L;
    private final List<PacketLogEntry> renderSnapshot = new ArrayList<>();
    private final List<PacketLogEntry> filtered = new ArrayList<>();
    private long snapshotChangeCounter = -1L;
    private String snapshotFilter;

    public PacketLog(BedrockDebuggerProxy proxy) {
        this.proxy = proxy;
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopFileLogging));
    }

    private boolean isLoggingEnabled() {
        SettingsConfig config = this.settingsConfig;
        if (config == null) {
            config = Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG)
                .get(SettingsConfig.class);
            this.settingsConfig = config;
        }
        return config == null || config.isPacketLogEnabled();
    }

    private boolean isNameExcluded(String name) {
        final SettingsConfig config = this.settingsConfig;
        if (config == null) {
            return false;
        }
        final List<String> excluded = config.getPacketLogExclusionList();
        if (excluded == null || excluded.isEmpty()) {
            return false;
        }
        for (final String excludedName : excluded) {
            if (name.equalsIgnoreCase(excludedName)) {
                return true;
            }
        }
        return false;
    }

    public void capture(BedrockPacketWrapper wrapper, PacketRecipient direction) {
        if (this.paused.get() || !this.isLoggingEnabled()) {
            return;
        }
        final BedrockPacket packet = wrapper.getPacket();
        final String name = packet.getClass().getSimpleName();
        if (this.isNameExcluded(name)) {
            return;
        }
        final ByteBuf buffer = wrapper.getPacketBuffer();
        final byte[] payload = buffer == null ? new byte[0] : ByteBufUtil.getBytes(buffer);

        PacketRecipient definitionRecipient = direction;
        int packetId = wrapper.getPacketId();
        try {
            final BedrockPacketDefinition<?> definition = NetworkConstants.CODEC
                .getPacketDefinition(packet.getClass());
            if (definition != null) {
                definitionRecipient = definition.getRecipient();
                packetId = definition.getId();
            }
        } catch (Exception ignored) {
        }

        String packetString = null;
        if (this.logToFile.get() || this.captureToString.get()) {
            try {
                packetString = packet.toString();
            } catch (Exception e) {
                packetString = "<toString failed: " + e + ">";
            }
        }

        final PacketLogEntry entry = new PacketLogEntry(
            this.sequence.getAndIncrement(),
            System.currentTimeMillis(),
            direction,
            definitionRecipient,
            packetId,
            name,
            payload,
            packetString
        );

        synchronized (this.entriesLock) {
            this.entries.addLast(entry);
            while (this.entries.size() > MAX_ENTRIES) {
                this.entries.removeFirst();
            }
            this.changeCounter++;
        }
        this.writeToFile(entry);
    }

    private void writeToFile(PacketLogEntry entry) {
        synchronized (this.fileLock) {
            if (this.fileWriter == null) {
                return;
            }
            try {
                this.fileWriter.write(String.format(
                    "[%s] [%s] id=%d (0x%02X) %s size=%d%n",
                    FILE_TIME_FORMAT.format(Instant.ofEpochMilli(entry.getTimestampMillis())),
                    this.directionArrow(entry.getDirection()),
                    entry.getPacketId(),
                    entry.getPacketId(),
                    entry.getName(),
                    entry.size()
                ));
                if (entry.getPacketString() != null) {
                    this.fileWriter.write(entry.getPacketString());
                    this.fileWriter.write(System.lineSeparator());
                }
                this.fileWriter.write(entry.getHexDump());
                this.fileWriter.write(System.lineSeparator());
                this.fileWriter.write(System.lineSeparator());
            } catch (IOException e) {
                this.proxy.getLogger().error("Failed to write packet to log file", e);
            }
        }
    }

    private void startFileLogging() {
        synchronized (this.fileLock) {
            this.closeWriterQuietly();
            final File folder = this.proxy.getDataLogsFolder();
            if (!folder.exists()) {
                folder.mkdirs();
            }
            final String name = "packets-" +
                FILE_NAME_FORMAT.format(Instant.now()) + ".log";
            final File file = new File(folder, name);
            try {
                this.fileWriter = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
                this.fileName = file.getPath();
                this.proxy.getLogger().info("Started logging packets to {}", this.fileName);
            } catch (IOException e) {
                this.proxy.getLogger().error("Failed to open packet log file", e);
                this.fileWriter = null;
                this.fileName = "";
                this.logToFile.set(false);
            }
        }
    }

    private void stopFileLogging() {
        synchronized (this.fileLock) {
            this.closeWriterQuietly();
            this.fileName = "";
        }
    }

    private void closeWriterQuietly() {
        if (this.fileWriter != null) {
            try {
                this.fileWriter.close();
            } catch (IOException ignored) {
            }
            this.fileWriter = null;
        }
    }

    public void clear() {
        synchronized (this.entriesLock) {
            this.entries.clear();
            this.changeCounter++;
        }
        this.selectedSequence = -1L;
    }

    public void render(BedrockDebuggerProxy proxy) {
        ImGui.setNextWindowSizeConstraints(500f, 300f, Float.MAX_VALUE, Float.MAX_VALUE);
        if (ImGui.begin("Packet Log", ImGuiWindowFlags.NoCollapse)) {
            proxy.getImGuiAdapter().trackWindow("packet_log");
            this.renderToolbar();
            this.renderTable();
        }
        ImGui.end();
    }

    private void renderToolbar() {
        ImGui.pushItemWidth(200f);
        ImGui.inputTextWithHint("##filter", "Filter by name", this.filter);
        ImGui.popItemWidth();
        ImGui.sameLine();
        ImGui.checkbox("Pause", this.paused);
        ImGui.sameLine();
        ImGui.checkbox("Auto-scroll", this.autoScroll);
        ImGui.sameLine();
        if (ImGui.button("Clear")) {
            this.clear();
        }
        ImGui.sameLine();
        if (ImGui.checkbox("Log to file", this.logToFile)) {
            if (this.logToFile.get()) {
                this.startFileLogging();
            } else {
                this.stopFileLogging();
            }
        }
        ImGui.sameLine();
        ImGui.checkbox("Capture toString", this.captureToString);

        final int count;
        synchronized (this.entriesLock) {
            count = this.entries.size();
        }
        ImGui.sameLine();
        ImGui.textColored(MUTED_COLOR, count + " packets");
        if (!this.isLoggingEnabled()) {
            ImGui.sameLine();
            ImGui.textColored(BOTH_COLOR, "(capture disabled in settings)");
        }
        if (this.logToFile.get() && !this.fileName.isEmpty()) {
            ImGui.textColored(MUTED_COLOR, "-> " + this.fileName);
        }
    }

    private void renderTable() {
        this.snapshotAndFilter();

        final float detailHeight = 220f;
        final float tableHeight = Math.max(120f, ImGui.getContentRegionAvailY() - detailHeight);

        final int tableFlags = ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg |
            ImGuiTableFlags.ScrollY | ImGuiTableFlags.Resizable | ImGuiTableFlags.Reorderable |
            ImGuiTableFlags.Hideable;

        if (ImGui.beginTable("packet_log_table", 6, tableFlags, 0f, tableHeight)) {
            ImGui.tableSetupScrollFreeze(0, 1);
            ImGui.tableSetupColumn("#", ImGuiTableColumnFlags.WidthFixed, 60f);
            ImGui.tableSetupColumn("Time", ImGuiTableColumnFlags.WidthFixed, 90f);
            ImGui.tableSetupColumn("Dir", ImGuiTableColumnFlags.WidthFixed, 55f);
            ImGui.tableSetupColumn("ID", ImGuiTableColumnFlags.WidthFixed, 45f);
            ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WidthStretch);
            ImGui.tableSetupColumn("Size", ImGuiTableColumnFlags.WidthFixed, 55f);
            ImGui.tableHeadersRow();

            final List<PacketLogEntry> rows = this.filtered;
            ImGuiListClipper.forEach(rows.size(), new ImListClipperCallback() {
                @Override
                public void accept(int index) {
                    PacketLog.this.renderRow(rows.get(index));
                }
            });

            if (this.autoScroll.get() && ImGui.getScrollY() >= ImGui.getScrollMaxY() - 1f) {
                ImGui.setScrollHereY(1.0f);
            }
            ImGui.endTable();
        }

        this.renderDetails();
    }

    private void renderRow(PacketLogEntry entry) {
        ImGui.tableNextRow();
        ImGui.tableNextColumn();
        final boolean selected = entry.getSequence() == this.selectedSequence;
        if (ImGui.selectable(String.valueOf(entry.getSequence()), selected,
            ImGuiSelectableFlags.SpanAllColumns)) {
            this.selectedSequence = entry.getSequence();
        }
        ImGui.tableNextColumn();
        ImGui.text(TABLE_TIME_FORMAT.format(Instant.ofEpochMilli(entry.getTimestampMillis())));
        ImGui.tableNextColumn();
        ImGui.textColored(this.directionColor(entry.getDirection()),
            this.directionArrow(entry.getDirection()));
        ImGui.tableNextColumn();
        ImGui.text(String.valueOf(entry.getPacketId()));
        ImGui.tableNextColumn();
        ImGui.text(entry.getName());
        ImGui.tableNextColumn();
        ImGui.text(String.valueOf(entry.size()));
    }

    private void renderDetails() {
        final PacketLogEntry entry = this.findSelected();
        if (ImGui.beginChild("packet_log_details", 0f, 0f, true)) {
            if (entry == null) {
                ImGui.textColored(MUTED_COLOR, "Select a packet to inspect its bytes");
            } else {
                ImGui.text(entry.getName());
                ImGui.sameLine();
                ImGui.textColored(MUTED_COLOR,
                    "  id=" + entry.getPacketId() +
                        String.format(" (0x%02X)", entry.getPacketId()) +
                        "  size=" + entry.size() + " bytes");

                ImGui.text("Direction: ");
                ImGui.sameLine();
                ImGui.textColored(
                    this.directionColor(entry.getDirection()),
                    entry.isServerbound() ? "Serverbound" : "Clientbound"
                );

                ImGui.text("Packet Recipient: ");
                ImGui.sameLine();
                ImGui.textColored(this.directionColor(entry.getDefinitionRecipient()),
                    entry.getDefinitionRecipient().name());

                ImGui.radioButton("Hex", this.viewMode, VIEW_HEX);
                ImGui.sameLine();
                ImGui.radioButton("Binary", this.viewMode, VIEW_BINARY);
                ImGui.sameLine();
                ImGui.radioButton("toString", this.viewMode, VIEW_STRING);
                ImGui.sameLine();
                if (ImGui.smallButton("Copy")) {
                    ImGui.setClipboardText(this.viewContent(entry));
                }

                ImGui.separator();
                final boolean wrap = this.viewMode.get() == VIEW_STRING;
                final int childFlags = wrap ? ImGuiWindowFlags.None
                    : ImGuiWindowFlags.HorizontalScrollbar;
                if (ImGui.beginChild("packet_log_bytes", 0f, 0f, false, childFlags)) {
                    if (wrap) {
                        ImGui.pushTextWrapPos(0f);
                        ImGui.textUnformatted(this.viewContent(entry));
                        ImGui.popTextWrapPos();
                    } else {
                        ImGui.textUnformatted(this.viewContent(entry));
                    }
                }
                ImGui.endChild();
            }
        }
        ImGui.endChild();
    }

    private String viewContent(PacketLogEntry entry) {
        return switch (this.viewMode.get()) {
            case VIEW_BINARY -> entry.getBinaryDump();
            case VIEW_STRING -> entry.getPacketString() == null
                ? "toString not captured - enable \"Capture toString\" or \"Log to file\""
                : entry.getPacketString();
            default -> entry.getHexDump();
        };
    }

    private PacketLogEntry findSelected() {
        if (this.selectedSequence < 0) {
            return null;
        }
        for (final PacketLogEntry entry : this.renderSnapshot) {
            if (entry.getSequence() == this.selectedSequence) {
                return entry;
            }
        }
        return null;
    }

    private void snapshotAndFilter() {
        final String needle = this.filter.get().toLowerCase();
        final long currentChange;
        synchronized (this.entriesLock) {
            currentChange = this.changeCounter;
            if (currentChange == this.snapshotChangeCounter && needle.equals(this.snapshotFilter)) {
                return;
            }
            this.renderSnapshot.clear();
            this.renderSnapshot.addAll(this.entries);
        }
        this.snapshotChangeCounter = currentChange;
        this.snapshotFilter = needle;
        this.filtered.clear();
        if (needle.isEmpty()) {
            this.filtered.addAll(this.renderSnapshot);
        } else {
            for (final PacketLogEntry entry : this.renderSnapshot) {
                if (entry.getName().toLowerCase().contains(needle)) {
                    this.filtered.add(entry);
                }
            }
        }
    }

    private String directionArrow(PacketRecipient recipient) {
        return switch (recipient) {
            case SERVER -> "C->S";
            case CLIENT -> "S->C";
            case BOTH -> "C<->S";
        };
    }

    private ImVec4 directionColor(PacketRecipient recipient) {
        return switch (recipient) {
            case SERVER -> SERVERBOUND_COLOR;
            case CLIENT -> CLIENTBOUND_COLOR;
            case BOTH -> BOTH_COLOR;
        };
    }
}