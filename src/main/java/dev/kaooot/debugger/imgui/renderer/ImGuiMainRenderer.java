package dev.kaooot.debugger.imgui.renderer;

import com.google.gson.JsonArray;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.imgui.ImGuiAdapter;
import dev.kaooot.debugger.level.LevelChunk;
import dev.kaooot.debugger.level.block.Block;
import dev.kaooot.debugger.player.CheatClientAuthority;
import dev.kaooot.debugger.player.ClientAuthoritativeSettings;
import dev.kaooot.debugger.player.DebugMarkerSettings;
import dev.kaooot.debugger.util.Util;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.data.payload.skin.PieceType;
import org.cloudburstmc.protocol.bedrock.data.payload.skin.SerializedPersonaPieceHandle;
import org.cloudburstmc.protocol.bedrock.data.payload.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.data.payload.skin.TintMapColor;
import org.cloudburstmc.protocol.bedrock.packet.AddActorPacket;
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerFogPacket;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ImGuiMainRenderer implements ImGuiRenderer {

    private BedrockDebuggerProxy proxy;

    private WeatherType selectedWeatherType = WeatherType.NONE;
    private ThunderstormIntensity selectedThunderstormIntensity = ThunderstormIntensity.NORMAL;
    private long thunderstormTick;
    private int selectedFogIndex = -1;

    private String[] fogIds;

    private final ImBoolean imGuiTabOpen = new ImBoolean(false);
    private final ImBoolean blockDebugTabOpen = new ImBoolean(false);
    private final ImBoolean packetLogTabOpen = new ImBoolean(false);
    private final ImBoolean weatherDebugTabOpen = new ImBoolean(false);
    private final ImBoolean personaDebugTabOpen = new ImBoolean(false);
    private final ImBoolean antiCheatTestingTabOpen = new ImBoolean(false);

    private static final ImVec4 DEFAULT_COLOR = new ImVec4(0.26f, 0.59f, 0.98f, 0.4f);
    private static final ImVec4 BLACK_COLOR = new ImVec4(0f, 0f, 0f, 1f);
    private static final ImVec4 LABEL_COLOR = new ImVec4(0.55f, 0.72f, 1f, 1f);
    private static final ImVec4 MUTED_COLOR = new ImVec4(0.6f, 0.6f, 0.6f, 1f);
    private static final ImVec4 GOOD_COLOR = new ImVec4(0.45f, 0.85f, 0.45f, 1f);
    private static final int DEFAULT_TEXT_COLOR = 0xFFFFFFFF;
    private static final int DEFAULT_TEXT_BACKGROUND_COLOR = 0xFF4296FA;

    private final ImString customBlockIdFilter = new ImString(100);

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
        proxy.getImGuiAdapter().removeWindow("custom_block_table");
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
            this.renderTab(this.packetLogTabOpen, "Packet Log");
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
                ImGui.text(
                    "Internal Chunk Count: " + this.proxy.getPlayer().getPlayerChunkManager()
                        .getChunks().size()
                );
                ImGui.text(
                    "Internal Primitive Shapes Count: " + this.proxy.getDebugShapeRenderer()
                        .getShapesCount()
                );
            }
            if (this.blockDebugTabOpen.get()) {
                this.renderBlockDebug();
            }
            if (this.packetLogTabOpen.get()) {
                this.proxy.getPacketLog().render(this.proxy);
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
    }

    private void renderTab(ImBoolean opened, String name) {
        ImGui.pushStyleColor(
            ImGuiCol.Button,
            opened.get() ? DEFAULT_COLOR : BLACK_COLOR
        );
        if (ImGui.button(name)) {
            opened.set(!opened.get());
        }
        ImGui.popStyleColor();
    }

    private void renderBlockDebug() {
        if (this.proxy.isTransferring()) {
            return;
        }
        if (ImGui.begin("Block Debug",
            ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoCollapse)) {
            this.proxy.getImGuiAdapter().trackWindow("block");
            final LevelChunk chunk = this.proxy.getPlayer().getPlayerChunkManager().getChunk();
            final Vector3i blockPos = this.proxy.getPlayer().getBlockBelow().sub(0f, 1f, 0f);
            final Block block = chunk.getBlock(blockPos);
            ImGui.text("Block Pos: " + this.proxy.getPlayer().getBlockBelow());
            ImGui.text("Block Runtime ID: " + block.getBlockRuntimeId());
            if (block.getState() != null) {
                ImGui.text("Block State: " + this.proxy.getGson().toJson(
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
        ImGui.setNextWindowSizeConstraints(0, 0, Float.MAX_VALUE, 400);
        if (ImGui.begin("Custom Block Table", ImGuiWindowFlags.NoCollapse)) {
            this.proxy.getImGuiAdapter().trackWindow("custom_block_table");

            ImGui.inputText("Filter", this.customBlockIdFilter);

            if (ImGui.beginTable("custom_block_table", 2,
                ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg)) {
                ImGui.tableSetupColumn("ID");
                ImGui.tableSetupColumn("Render Settings");
                ImGui.tableHeadersRow();

                final Map<String, DebugMarkerSettings> ids =
                    this.proxy.getPlayer().getCustomBlockRenderSettings();
                for (final String identifier : this.proxy.getBlockPaletteManager()
                    .getKnownCustomBlockIdentifiers()) {
                    if (!this.customBlockIdFilter.isEmpty() &&
                        !identifier.contains(this.customBlockIdFilter.get())) {
                        continue;
                    }
                    ImGui.pushID(identifier);
                    ImGui.tableNextRow();
                    ImGui.tableSetColumnIndex(0);
                    ImGui.text(identifier);
                    ImGui.tableSetColumnIndex(1);
                    final ImBoolean renderDebugMarker = new ImBoolean(
                        this.proxy.getPlayer().getCustomBlockRenderSettings()
                            .containsKey(identifier)
                    );

                    if (ImGui.checkbox("Render Debug Marker", renderDebugMarker)) {
                        final boolean value = renderDebugMarker.get();
                        if (value && !ids.containsKey(identifier)) {
                            final DebugMarkerSettings settings = new DebugMarkerSettings();
                            settings.setTextColor(DEFAULT_TEXT_COLOR);
                            settings.setTextBackgroundColor(DEFAULT_TEXT_BACKGROUND_COLOR);

                            ids.put(identifier, settings);

                            this.updateAllChunksAsync();
                        } else if (!value && ids.containsKey(identifier)) {
                            ids.remove(identifier);
                            this.proxy.getDebugShapeRenderer().clearShapes(
                                s -> s.startsWith("debug_marker_" + identifier)
                            );
                        }
                    }
                    final boolean isPresent = ids.containsKey(identifier);
                    if (isPresent) {
                        final DebugMarkerSettings settings = ids.get(identifier);
                        float[] textColorComponents = Util.toFloats(settings.getTextColor());
                        if (ImGui.colorEdit4("Text Color", textColorComponents,
                            ImGuiColorEditFlags.NoInputs)) {
                            settings.setTextColor(Util.fromFloats(textColorComponents));
                        }

                        float[] textBgColorComponents = Util.toFloats(
                            settings.getTextBackgroundColor()
                        );
                        if (ImGui.colorEdit4("Background Color", textBgColorComponents,
                            ImGuiColorEditFlags.NoInputs)) {
                            settings.setTextBackgroundColor(Util.fromFloats(textBgColorComponents));
                        }
                        if (ImGui.button("Update Colors")) {
                            this.updateAllChunksAsync();
                        }
                    }
                    ImGui.popID();
                }
                ImGui.endTable();
            }
        }
        ImGui.end();
    }

    private void updateAllChunksAsync() {
        CompletableFuture.runAsync(() -> {
            for (final LevelChunk chunk : this.proxy.getPlayer().getPlayerChunkManager()
                .getChunks().values()) {
                this.proxy.getPlayer().updateCustomBlockDebugMarkers(chunk);
            }
        });
    }

    private void renderAntiCheatTestingSection() {
        ImGui.setNextWindowSizeConstraints(340f, 200f, Float.MAX_VALUE, Float.MAX_VALUE);
        if (ImGui.begin("AntiCheat Testing", ImGuiWindowFlags.NoCollapse)) {
            this.proxy.getImGuiAdapter().trackWindow("ac");
            final CheatClientAuthority authority = this.proxy.getPlayer().getCheatClientAuthority();
            final ClientAuthoritativeSettings settings = authority.getSettings();

            ImGui.separatorText("Block");
            this.checkboxToggle("Force Mine Ability", settings.isForceMineAbilityEnabled(),
                value -> {
                    settings.setForceMineAbilityEnabled(value);
                    authority.updateMineAbility();
                });
            this.checkboxToggle("Bypass Invalid Creative Destroy Action",
                settings.isBypassInvalidCreativeDestroyAction(),
                settings::setBypassInvalidCreativeDestroyAction);
            this.helpMarker("Test the invalid creative destroy action with: " +
                "/client_gametype creative");

            ImGui.spacing();
            ImGui.textColored(LABEL_COLOR, "Nuker");
            ImGui.indent();
            this.checkboxToggle(
                "Enabled##nuker",
                settings.isNukerEnabled(),
                settings::setNukerEnabled
            );
            ImGui.beginDisabled(!settings.isNukerEnabled());
            ImGui.setNextItemWidth(140f);
            final ImInt nukerWidth = new ImInt(settings.getNukerWidth());
            if (ImGui.inputInt("Width", nukerWidth, 0, 0, ImGuiInputTextFlags.None)) {
                settings.setNukerWidth(nukerWidth.get());
            }
            ImGui.setNextItemWidth(140f);
            final ImInt nukerHeight = new ImInt(settings.getNukerHeight());
            if (ImGui.inputInt("Height", nukerHeight, 0, 0, ImGuiInputTextFlags.None)) {
                settings.setNukerHeight(nukerHeight.get());
            }
            ImGui.endDisabled();
            ImGui.unindent();

            ImGui.separatorText("Combat");
            ImGui.setNextItemWidth(140f);
            final ImFloat interactionRange = new ImFloat(settings.getActorInteractionRange());
            if (ImGui.inputFloat("Interaction Reach", interactionRange, 0, 0,
                ImGuiInputTextFlags.None)) {
                settings.setActorInteractionRange(interactionRange.get());
            }
            ImGui.setNextItemWidth(140f);
            final ImFloat attackRange = new ImFloat(settings.getActorAttackRange());
            if (ImGui.inputFloat("Attack Reach", attackRange, 0, 0, ImGuiInputTextFlags.None)) {
                settings.setActorAttackRange(attackRange.get());
            }

            ImGui.spacing();
            this.checkboxToggle("CPS Override", settings.isCpsOverrideEnabled(),
                settings::setCpsOverrideEnabled);
            ImGui.beginDisabled(!settings.isCpsOverrideEnabled());
            ImGui.setNextItemWidth(140f);
            final ImInt clicksPerSecond = new ImInt(settings.getClicksPerSecond());
            if (ImGui.inputInt("Clicks Per Second", clicksPerSecond, 0, 0,
                ImGuiInputTextFlags.None)) {
                settings.setClicksPerSecond(clicksPerSecond.get());
            }
            ImGui.endDisabled();
        }
        ImGui.end();
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
        ImGui.setNextWindowSizeConstraints(380f, 220f, Float.MAX_VALUE, Float.MAX_VALUE);
        if (ImGui.begin("Persona Debug", ImGuiWindowFlags.NoCollapse)) {
            this.proxy.getImGuiAdapter().trackWindow("persona");

            ImGui.separatorText("Toggles");
            if (ImGui.checkbox("Force Enable Persona Skins", config.isForceEnablePersonaSkins())) {
                config.setForceEnablePersonaSkins(!config.isForceEnablePersonaSkins());
                configRegistry.save(config);
            }
            this.helpMarker(
                "Override for the PersonaDisabled StartGame and PersonaSkin Login booleans."
            );

            final SerializedSkin skin = this.proxy.getPlayer().getSerializedSkin();

            ImGui.separatorText("Skin Overview");
            if (ImGui.beginTable("persona_overview", 2,
                ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg | ImGuiTableFlags.SizingFixedFit)) {
                ImGui.tableSetupColumn("Property", ImGuiTableColumnFlags.WidthFixed, 150f);
                ImGui.tableSetupColumn("Value", ImGuiTableColumnFlags.WidthStretch);

                this.kvBool("Persona", skin.isPersona());
                this.kvBool("Premium", skin.isPremium());
                this.kvBool("Overrides Appearance", skin.isOverridesPlayerAppearance());
                this.kvText("Skin ID", skin.getID());
                this.kvText("Full ID", skin.getFullID());
                this.kvText("Cape ID", skin.getCapeID());
                this.kvText(
                    "Arm Size",
                    skin.getArmSize() == null ? null : skin.getArmSize().name()
                );
                this.kvText("Play Fab ID", skin.getPlayFabID());

                ImGui.tableNextRow();
                ImGui.tableSetColumnIndex(0);
                ImGui.textColored(LABEL_COLOR, "Skin Color");
                ImGui.tableSetColumnIndex(1);
                this.colorSwatch("##skin_color", skin.getSkinColor());
                ImGui.sameLine();
                ImGui.text(String.format("#%08X", skin.getSkinColor()));
                ImGui.endTable();
            }

            if (skin.isPersona()) {
                ImGui.separatorText("Persona Pieces (" + skin.getPersonaPieces().size() + ")");
                if (ImGui.beginTable("persona_pieces_table", 4,
                    ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg |
                        ImGuiTableFlags.ScrollY, 0f, 160f)) {
                    ImGui.tableSetupScrollFreeze(0, 1);
                    ImGui.tableSetupColumn("Type", ImGuiTableColumnFlags.WidthFixed, 110f);
                    ImGui.tableSetupColumn("Piece ID", ImGuiTableColumnFlags.WidthStretch);
                    ImGui.tableSetupColumn("Default", ImGuiTableColumnFlags.WidthFixed, 60f);
                    ImGui.tableSetupColumn("Product ID", ImGuiTableColumnFlags.WidthStretch);
                    ImGui.tableHeadersRow();

                    for (final SerializedPersonaPieceHandle handle : skin.getPersonaPieces()) {
                        ImGui.tableNextRow();
                        ImGui.tableSetColumnIndex(0);
                        ImGui.text(handle.getPieceType().getId());
                        ImGui.tableSetColumnIndex(1);
                        ImGui.textUnformatted(this.orDash(handle.getPieceId()));
                        ImGui.tableSetColumnIndex(2);
                        ImGui.textColored(handle.isDefaultPiece() ? GOOD_COLOR : MUTED_COLOR,
                            handle.isDefaultPiece() ? "Yes" : "No");
                        ImGui.tableSetColumnIndex(3);
                        ImGui.textUnformatted(this.orDash(handle.getProductId()));
                    }
                    ImGui.endTable();
                }

                final Map<PieceType, TintMapColor> tints = skin.getPieceTintColors();
                if (!tints.isEmpty()) {
                    ImGui.separatorText("Piece Tint Colors");
                    if (ImGui.beginTable("persona_tints_table", 2,
                        ImGuiTableFlags.Borders | ImGuiTableFlags.RowBg)) {
                        ImGui.tableSetupColumn("Type", ImGuiTableColumnFlags.WidthFixed, 110f);
                        ImGui.tableSetupColumn("Colors", ImGuiTableColumnFlags.WidthStretch);
                        ImGui.tableHeadersRow();

                        for (final Map.Entry<PieceType, TintMapColor> entry : tints.entrySet()) {
                            ImGui.tableNextRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text(entry.getKey().getId());
                            ImGui.tableSetColumnIndex(1);
                            final List<Integer> colors = entry.getValue().getColors();
                            if (colors.isEmpty()) {
                                ImGui.textDisabled("-");
                            }
                            for (int i = 0; i < colors.size(); i++) {
                                if (i > 0) {
                                    ImGui.sameLine();
                                }
                                this.colorSwatch(
                                    "##tint_" + entry.getKey().getId() + "_" + i, colors.get(i)
                                );
                            }
                        }
                        ImGui.endTable();
                    }
                }
            } else {
                ImGui.spacing();
                ImGui.textDisabled("This player is not using a persona skin.");
            }
        }
        ImGui.end();
    }

    private void checkboxToggle(String label, boolean current, Consumer<Boolean> setter) {
        if (ImGui.checkbox(label, current)) {
            setter.accept(!current);
        }
    }

    private void helpMarker(String text) {
        ImGui.sameLine();
        ImGui.textDisabled("(?)");
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(text);
        }
    }

    private void kvText(String key, String value) {
        ImGui.tableNextRow();
        ImGui.tableSetColumnIndex(0);
        ImGui.textColored(LABEL_COLOR, key);
        ImGui.tableSetColumnIndex(1);
        ImGui.textUnformatted(this.orDash(value));
    }

    private void kvBool(String key, boolean value) {
        ImGui.tableNextRow();
        ImGui.tableSetColumnIndex(0);
        ImGui.textColored(LABEL_COLOR, key);
        ImGui.tableSetColumnIndex(1);
        ImGui.textColored(value ? GOOD_COLOR : MUTED_COLOR, value ? "Yes" : "No");
    }

    private void colorSwatch(String id, int argb) {
        final int[] rgba = Util.toRgba(argb);
        ImGui.colorButton(id,
            new ImVec4(rgba[0] / 255f, rgba[1] / 255f, rgba[2] / 255f, 1f),
            ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoAlpha,
            new ImVec2(18f, 18f));
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(String.format("#%08X (r=%d g=%d b=%d a=%d)",
                argb, rgba[0], rgba[1], rgba[2], rgba[3]));
        }
    }

    private String orDash(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }
}