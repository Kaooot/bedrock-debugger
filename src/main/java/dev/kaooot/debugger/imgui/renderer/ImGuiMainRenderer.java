package dev.kaooot.debugger.imgui.renderer;

import com.google.gson.JsonArray;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import io.netty.util.AbstractReferenceCounted;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketDefinition;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.data.payload.skin.PieceType;
import org.cloudburstmc.protocol.bedrock.data.payload.skin.SerializedPersonaPieceHandle;
import org.cloudburstmc.protocol.bedrock.data.payload.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.data.payload.skin.TintMapColor;
import org.cloudburstmc.protocol.bedrock.packet.AddActorPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketType;
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerFogPacket;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.imgui.ImGuiAdapter;
import dev.kaooot.debugger.level.LevelChunk;
import dev.kaooot.debugger.level.block.Block;
import dev.kaooot.debugger.network.NetworkConstants;
import dev.kaooot.debugger.player.CheatClientAuthority;
import dev.kaooot.debugger.player.ClientAuthoritativeSettings;
import dev.kaooot.debugger.util.Util;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ImGuiMainRenderer implements ImGuiRenderer {

    private BedrockDebuggerProxy proxy;

    public static int SOUND_EVENT_DEBUG_COUNTER = 10;
    public static boolean SOUND_EVENT_DEBUG_LOCKED = false;

    private final ImString filter = new ImString(100);
    private WeatherType selectedWeatherType = WeatherType.NONE;
    private ThunderstormIntensity selectedThunderstormIntensity = ThunderstormIntensity.NORMAL;
    private long thunderstormTick;
    private int selectedFogIndex = -1;

    private final Map<BedrockPacketType, BedrockPacket> packets =
        new Object2ObjectOpenHashMap<>();

    private String[] fogIds;

    private final ImBoolean imGuiTabOpen = new ImBoolean(false);
    private final ImBoolean blockDebugTabOpen = new ImBoolean(false);
    private final ImBoolean levelSoundEventDebugTabOpen = new ImBoolean(false);
    private final ImBoolean packetLogTabOpen = new ImBoolean(false);
    private final ImBoolean weatherDebugTabOpen = new ImBoolean(false);
    private final ImBoolean personaDebugTabOpen = new ImBoolean(false);
    private final ImBoolean antiCheatTestingTabOpen = new ImBoolean(false);
    private final ImVec4 defaultColor = new ImVec4(0.26f, 0.59f, 0.98f, 0.4f);
    private final ImVec4 blackColor = new ImVec4(0f, 0f, 0f, 1f);

    @Override
    public void render(BedrockDebuggerProxy proxy, ImGuiAdapter adapter) {
        this.proxy = proxy;
        final ConfigRegistry configRegistry = Registries.getRegistry(RegistryKey.CONFIG);
        final SettingsConfig settingsConfig = configRegistry.get(SettingsConfig.class);
        if (this.fogIds == null) {
            try (final InputStream inputStream = proxy.getClass().getClassLoader()
                .getResourceAsStream("fog_identifiers.json")) {
                final JsonArray array = this.proxy.getGson()
                    .fromJson(new String(inputStream.readAllBytes()), JsonArray.class);
                this.fogIds = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    this.fogIds[i] = array.get(i).getAsString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        proxy.getImGuiAdapter().removeWindow("main");
        proxy.getImGuiAdapter().removeWindow("block");
        proxy.getImGuiAdapter().removeWindow("levelSoundEvent");
        proxy.getImGuiAdapter().removeWindow("ac");
        proxy.getImGuiAdapter().removeWindow("packet_log");
        proxy.getImGuiAdapter().removeWindow("weather");
        proxy.getImGuiAdapter().removeWindow("persona");
        proxy.getImGuiAdapter().removeWindow("debug1");

        ImGui.setNextWindowBgAlpha(1.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        if (ImGui.begin("Debug", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize |
            ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoSavedSettings |
            ImGuiWindowFlags.AlwaysAutoResize)) {
            proxy.getImGuiAdapter().trackWindow("main");
            this.renderTab(this.imGuiTabOpen, "ImGui");
            ImGui.sameLine();
            this.renderTab(this.blockDebugTabOpen, "Block Debug");
            ImGui.sameLine();
            this.renderTab(this.levelSoundEventDebugTabOpen, "LevelSoundEvent Debug");
//            ImGui.sameLine();
//            this.renderTab(this.packetLogTabOpen, "Packet Log");
            ImGui.sameLine();
            this.renderTab(this.weatherDebugTabOpen, "Weather Debug");
            ImGui.sameLine();
            this.renderTab(this.personaDebugTabOpen, "Persona Debug");
            ImGui.sameLine();
            this.renderTab(this.antiCheatTestingTabOpen, "AntiCheat Testing");
            if (this.imGuiTabOpen.get()) {
                ImGui.spacing();
                ImGui.separator();
                if (ImGui.button("Force Reload")) {
                    this.proxy.getImGuiAdapter().reinit();
                }
            }
            if (this.blockDebugTabOpen.get()) {
                this.renderBlockDebug();
            }
            if (this.levelSoundEventDebugTabOpen.get()) {
                this.renderLevelSoundEventDebug();
            }
            if (this.packetLogTabOpen.get()) {
//                this.renderPacketLog();
            }
            if (this.weatherDebugTabOpen.get()) {
                this.renderWeatherDebug();
            }
            if (this.personaDebugTabOpen.get()) {
                this.renderPersonaDebug(configRegistry, settingsConfig);
            }
            if (this.antiCheatTestingTabOpen.get()) {
                this.renderAntiCheatTestingSection();
            }
        }
        ImGui.end();
        ImGui.popStyleVar();
        //ImGui.showDemoWindow();
    }

    private void renderTab(ImBoolean opened, String name) {
        ImGui.pushStyleColor(
            ImGuiCol.Button,
            opened.get() ? this.defaultColor : this.blackColor
        );
        if (ImGui.button(name)) {
            opened.set(!opened.get());
        }
        ImGui.popStyleColor();
    }

    private void renderBlockDebug() {
        if (ImGui.begin("Block Debug",
            ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoCollapse)) {
            this.proxy.getImGuiAdapter().trackWindow("block");
            final LevelChunk chunk = this.proxy.getPlayer().getPlayerChunkManager().getChunk();
            final Vector3i blockPos = this.proxy.getPlayer().getBlockBelow().sub(0f, 1f, 0f);
            final Block block = chunk.getBlock(blockPos);
            ImGui.text("BlockPos: " + this.proxy.getPlayer().getBlockBelow());
            ImGui.text("BlockRuntimeID: " + block.getBlockRuntimeId());
            if (block.getState() != null) {
                ImGui.text("State: " + this.proxy.getGson().toJson(
                    this.proxy.getBlockPaletteManager().getBlockStateAsJSON(block.getState())
                ));
            }
            if (chunk.getBlockActorData(blockPos) != null) {
                ImGui.text(
                    "Block Actor Data Tags: " +
                        this.proxy.getGson().toJson(
                            Util.convertCompoundToJson(chunk.getBlockActorData(blockPos))
                        )
                );
            }
        }
        ImGui.end();
    }

    private void renderLevelSoundEventDebug() {
        if (ImGui.begin("LevelSoundEvent Debug",
            ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoCollapse)) {
            this.proxy.getImGuiAdapter().trackWindow("levelSoundEvent");
            final ImInt count = new ImInt(SOUND_EVENT_DEBUG_COUNTER);
            if (ImGui.inputInt("Max Count", count, 0, 0, ImGuiInputTextFlags.None)) {
                SOUND_EVENT_DEBUG_COUNTER = count.get();
            }
            if (ImGui.checkbox("Locked", SOUND_EVENT_DEBUG_LOCKED)) {
                SOUND_EVENT_DEBUG_LOCKED = !SOUND_EVENT_DEBUG_LOCKED;
            }

            if (ImGui.beginTable("Level Sound Events", 2,
                ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg)) {
                ImGui.tableSetupColumn("Sound Event");
                ImGui.tableSetupColumn("Position");
                ImGui.tableHeadersRow();

                int counter = 0;
                for (final LevelSoundEventPacket levelSoundEventPacket : this.proxy.getPlayer()
                    .getLevelSoundEventPackets()) {
                    if (counter >= count.get()) {
                        break;
                    }
                    ImGui.tableNextRow();
                    ImGui.tableSetColumnIndex(0);
                    ImGui.text(levelSoundEventPacket.getSoundEvent().getId());
                    ImGui.tableSetColumnIndex(1);
                    ImGui.text(levelSoundEventPacket.getPosition().toInt().toString());
                    counter++;
                }
                ImGui.endTable();
            }
        }
        ImGui.end();
    }

    private void renderAntiCheatTestingSection() {
        ImGui.setNextWindowSizeConstraints(200f, 100f, Float.MAX_VALUE, Float.MAX_VALUE);
        if (ImGui.begin("AntiCheat Testing",
            ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoCollapse)) {
            this.proxy.getImGuiAdapter().trackWindow("ac");
            final CheatClientAuthority authority = this.proxy.getPlayer().getCheatClientAuthority();
            final ClientAuthoritativeSettings settings = authority.getSettings();

            if (ImGui.collapsingHeader("Block")) {
                ImGui.text(
                    "Invalid Creative Destroy Action can be tested with: /client_gametype creative"
                );
                if (ImGui.checkbox("Force Mine Ability Enabled",
                    settings.isForceMineAbilityEnabled())) {
                    settings.setForceMineAbilityEnabled(!settings.isForceMineAbilityEnabled());
                    authority.updateMineAbility();
                }
                if (ImGui.checkbox("Bypass Invalid Creative Destroy Action",
                    settings.isBypassInvalidCreativeDestroyAction())) {
                    settings.setBypassInvalidCreativeDestroyAction(
                        !settings.isBypassInvalidCreativeDestroyAction()
                    );
                }
                if (ImGui.collapsingHeader("Nuker")) {
                    if (ImGui.checkbox("Enabled", settings.isNukerEnabled())) {
                        settings.setNukerEnabled(!settings.isNukerEnabled());
                    }
                    final ImInt nukerWidth = new ImInt(settings.getNukerWidth());
                    if (ImGui.inputInt("Width", nukerWidth, 0, 0, ImGuiInputTextFlags.None)) {
                        settings.setNukerWidth(nukerWidth.get());
                    }
                    final ImInt nukerHeight = new ImInt(settings.getNukerHeight());
                    if (ImGui.inputInt("Height", nukerHeight, 0, 0, ImGuiInputTextFlags.None)) {
                        settings.setNukerHeight(nukerHeight.get());
                    }
                }
            }
            if (ImGui.collapsingHeader("Combat")) {
                final ImFloat interactionRange = new ImFloat(settings.getActorInteractionRange());
                if (ImGui.inputFloat("Interaction Reach", interactionRange, 0, 0,
                    ImGuiInputTextFlags.None)) {
                    settings.setActorInteractionRange(interactionRange.get());
                }
                final ImFloat attackRange = new ImFloat(settings.getActorAttackRange());
                if (ImGui.inputFloat("Attack Reach", attackRange, 0, 0, ImGuiInputTextFlags.None)) {
                    settings.setActorAttackRange(attackRange.get());
                }
                if (ImGui.checkbox("CPS Override Enabled", settings.isCpsOverrideEnabled())) {
                    settings.setCpsOverrideEnabled(!settings.isCpsOverrideEnabled());
                }
                final ImInt clicksPerSecond = new ImInt(settings.getClicksPerSecond());
                if (ImGui.inputInt("Clicks Per Second", clicksPerSecond, 0, 0,
                    ImGuiInputTextFlags.None)) {
                    settings.setClicksPerSecond(clicksPerSecond.get());
                }
            }
        }
        ImGui.end();
    }

    private void renderPacketLog() {
        ImGui.setNextWindowSizeConstraints(0f, 500f, 600f, 500);
        if (ImGui.begin("Packet Log", ImGuiWindowFlags.NoCollapse)) {
            this.proxy.getImGuiAdapter().trackWindow("packet_log");

            ImGui.inputText("Filter", this.filter);

            if (ImGui.beginTable("Packets", 4,
                ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg)) {
                ImGui.tableSetupColumn("Recipient", ImGuiTableColumnFlags.WidthFixed, 70.0f);
                ImGui.tableSetupColumn("ID", ImGuiTableColumnFlags.WidthFixed, 50.0f);
                ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WidthFixed, 250.0f);
                ImGui.tableSetupColumn("View", ImGuiTableColumnFlags.WidthFixed, 150.0f);
                ImGui.tableHeadersRow();

                final List<Map.Entry<BedrockPacketType, BedrockPacket>> list = this.packets
                    .entrySet()
                    .stream()
                    .filter(e -> this.filter.isEmpty() ||
                        e.getValue().getClass().getSimpleName().toLowerCase()
                            .contains(this.filter.get().toLowerCase()))
                    .sorted(
                        (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getKey().name(),
                            o2.getKey().name())
                    ).toList();

                for (final Map.Entry<BedrockPacketType, BedrockPacket> entry : list) {
                    final BedrockPacket packet = entry.getValue();
                    final BedrockPacketDefinition<?> definition = NetworkConstants.CODEC
                        .getPacketDefinition(packet.getClass());
                    ImGui.tableNextRow();
                    ImGui.tableSetColumnIndex(0);
                    ImGui.text(definition.getRecipient().name());
                    ImGui.tableSetColumnIndex(1);
                    ImGui.text(String.valueOf(definition.getId()));
                    ImGui.tableSetColumnIndex(2);
                    ImGui.text(packet.getClass().getSimpleName());
                    ImGui.tableSetColumnIndex(3);
                    if (ImGui.button("Copy to Clipboard##" + entry.getKey().name())) {
                        ImGui.setClipboardText(packet.toString());
                    }
                }
                ImGui.endTable();
            }
        }
        ImGui.end();
    }

    public void logPacket(BedrockPacket packet) {
        if (packet instanceof AbstractReferenceCounted) {
            return;
        }
        this.packets.put(packet.getPacketType(), packet);
    }

    private void renderWeatherDebug() {
        if (ImGui.begin("Weather Debug",
            ImGuiWindowFlags.NoCollapse)) {
            this.proxy.getImGuiAdapter().trackWindow("weather");

            ImGui.text("Client weather debugging");
            ImGui.spacing();

            final ImInt selectedWeatherType = new ImInt(this.selectedWeatherType.ordinal());
            if (ImGui.combo("Weather Type Override", selectedWeatherType, WeatherType.getTypes())) {
                this.selectedWeatherType = WeatherType.VALUES[selectedWeatherType.get()];
            }

            switch (this.selectedWeatherType) {
                case CLEAR -> {
                    this.sendLevelEvent(LevelEvent.STOP_RAINING, 0);
                    this.sendLevelEvent(LevelEvent.STOP_THUNDERSTORM, 0);
                    this.thunderstormTick = 0L;
                }
                case RAIN -> this.sendLevelEvent(LevelEvent.START_RAINING, Integer.MAX_VALUE);
                case THUNDERSTORM -> {
                    this.sendLevelEvent(LevelEvent.START_THUNDERSTORM, Integer.MAX_VALUE);

                    final ImInt selectedThunderstormIntensity = new ImInt(
                        this.selectedThunderstormIntensity.ordinal()
                    );
                    ImGui.spacing();
                    if (ImGui.combo("Thunderstorm Intensity", selectedThunderstormIntensity,
                        ThunderstormIntensity.getTypes())) {
                        this.selectedThunderstormIntensity = ThunderstormIntensity.VALUES
                            [selectedThunderstormIntensity.get()];
                    }
                    ImGui.separator();
                    switch (this.selectedThunderstormIntensity) {
                        case NORMAL -> {
                            if (this.thunderstormTick % 1500 == 0) {
                                this.summonLightningStrike();
                            }
                        }
                        case ADVANCED -> {
                            if (this.thunderstormTick % 500 == 0) {
                                this.summonLightningStrike();
                            }
                        }
                        case EXTREME -> {
                            if (this.thunderstormTick % 50 == 0) {
                                this.summonLightningStrike();
                            }
                        }
                    }
                    this.thunderstormTick++;
                }
            }
            final ImInt selectedFog =
                new ImInt(this.selectedFogIndex == -1 ? 0 : this.selectedFogIndex);
            if (ImGui.combo("Fog Override", selectedFog, this.fogIds)) {
                this.selectedFogIndex = selectedFog.get();
            }
            if (this.selectedFogIndex != -1) {
                if (ImGui.button("Clear Fog Stack")) {
                    this.selectedFogIndex = -1;
                }
            }
            final PlayerFogPacket packet = new PlayerFogPacket();
            if (this.selectedFogIndex != -1) {
                packet.getFogStack().add(this.fogIds[this.selectedFogIndex]);
            }
            this.proxy.getServer().sendPacket(packet);
        }
        ImGui.end();
    }

    private void sendLevelEvent(LevelEvent type, int data) {
        final LevelEventPacket packet = new LevelEventPacket();
        packet.setType(type);
        packet.setPosition(this.proxy.getPlayer().getPosition());
        packet.setData(data);

        this.proxy.getServer().sendPacket(packet);
    }

    private void summonLightningStrike() {
        final Random random = ThreadLocalRandom.current();
        final long id = random.nextLong();
        final AddActorPacket packet = new AddActorPacket();
        packet.setTargetActorID(id);
        packet.setTargetRuntimeID(id);
        packet.setActorType("minecraft:lightning_bolt");
        packet.setPosition(this.proxy.getPlayer().getPosition()
            .add(
                random.nextInt(-256, 256),
                0,
                random.nextInt(-256, 256)
            )
        );
        packet.setVelocity(Vector3f.ZERO);
        packet.setRotation(Vector2f.ZERO);
        this.proxy.getServer().sendPacket(packet);
    }

    @Getter
    @RequiredArgsConstructor
    private enum WeatherType {
        NONE("None"),
        CLEAR("Clear"),
        RAIN("Rain"),
        THUNDERSTORM("Thunderstorm");

        private static final WeatherType[] VALUES = values();
        private static String[] TYPES;

        private final String id;

        private static String[] getTypes() {
            if (TYPES != null) {
                return TYPES;
            }
            TYPES = new String[VALUES.length];
            for (int i = 0; i < VALUES.length; i++) {
                TYPES[i] = VALUES[i].getId();
            }
            return TYPES;
        }
    }

    @Getter
    @RequiredArgsConstructor
    private enum ThunderstormIntensity {
        NORMAL("Normal"),
        ADVANCED("Advanced"),
        EXTREME("Extreme");

        private static final ThunderstormIntensity[] VALUES = values();
        private static String[] TYPES;

        private final String id;

        private static String[] getTypes() {
            if (TYPES != null) {
                return TYPES;
            }
            TYPES = new String[VALUES.length];
            for (int i = 0; i < VALUES.length; i++) {
                TYPES[i] = VALUES[i].getId();
            }
            return TYPES;
        }
    }

    private void renderPersonaDebug(ConfigRegistry configRegistry, SettingsConfig config) {
        if (ImGui.begin("Persona Debug", ImGuiWindowFlags.NoCollapse)) {
            this.proxy.getImGuiAdapter().trackWindow("persona");

            if (ImGui.checkbox("Force Enable Persona Skins", config.isForceEnablePersonaSkins())) {
                config.setForceEnablePersonaSkins(!config.isForceEnablePersonaSkins());
                configRegistry.save(config);
            }
            if (ImGui.isItemHovered()) {
                ImGui.beginTooltip();
                ImGui.text(
                    "Override for PersonaDisabled StartGame and PersonaSkin Login booleans"
                );
                ImGui.endTooltip();
            }

            final SerializedSkin serializedSkin = this.proxy.getPlayer().getSerializedSkin();
            if (serializedSkin.isPersona()) {
                ImGui.text("Persona Pieces");
                if (ImGui.beginTable("Persona Pieces", 2,
                    ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg)) {
                    ImGui.tableSetupColumn("Piece Id");
                    ImGui.tableSetupColumn("Piece Type");
                    ImGui.tableHeadersRow();

                    for (final SerializedPersonaPieceHandle handle : serializedSkin.getPersonaPieces()) {
                        ImGui.tableNextRow();
                        ImGui.tableSetColumnIndex(0);
                        ImGui.text(handle.getPieceId());
                        ImGui.tableSetColumnIndex(1);
                        ImGui.text(handle.getPieceType().getId());
                    }
                    ImGui.endTable();
                }

                ImGui.text("Piece Tint Colors");
                for (final Map.Entry<PieceType, TintMapColor> entry : serializedSkin.getPieceTintColors()
                    .entrySet()) {
                    ImGui.text(entry.getKey().getId() + " " + entry.getValue().getColors());
                }
            }
        }
        ImGui.end();
    }
}