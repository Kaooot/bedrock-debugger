package dev.kaooot.debugger.menu;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.actor.Actor;
import dev.kaooot.debugger.api.forms.CustomForm;
import dev.kaooot.debugger.api.forms.FormListener;
import dev.kaooot.debugger.api.forms.element.Divider;
import dev.kaooot.debugger.api.forms.element.Element;
import dev.kaooot.debugger.api.forms.element.Header;
import dev.kaooot.debugger.api.forms.element.Input;
import dev.kaooot.debugger.api.forms.element.Label;
import dev.kaooot.debugger.api.forms.element.StepSlider;
import dev.kaooot.debugger.api.forms.element.Toggle;
import dev.kaooot.debugger.api.forms.response.CustomResponse;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.config.TestConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.player.ServerPlayer;
import dev.kaooot.debugger.util.DebugElement;
import dev.kaooot.debugger.util.Util;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.File;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.cloudburstmc.protocol.bedrock.data.PlatformType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ServerSettingsMenu implements FormMenu<BedrockDebuggerProxy> {

    @Override
    public void show(BedrockDebuggerProxy proxy) {
        final List<Element> elements = new ObjectArrayList<>();
        final ConfigRegistry configRegistry = Registries.getRegistry(RegistryKey.CONFIG);
        final SettingsConfig settingsConfig = configRegistry.get(SettingsConfig.class);
        final TestConfig testConfig = configRegistry.get(TestConfig.class);

        this.addSettingsSection(settingsConfig, elements);
        this.addDebugSection(testConfig, elements);

        final FormListener<CustomResponse> listener =
            proxy.getPlayer().showForm(
                CustomForm.builder()
                    .title(DebugElement.WIDE_SERVER_FORM.getKey() + "Settings")
                    .build()
                    .add(elements.toArray(Element[]::new))
            );
        listener.onResponse(response -> {
            this.handleSettingsResponse(response, settingsConfig, configRegistry, proxy);
            this.handleDebugResponse(response, testConfig, configRegistry, proxy);
        });
    }

    public void addSettingsSection(SettingsConfig settingsConfig, List<Element> elements) {
        final Header header = new Header();
        header.setId("header-settings");
        header.setText("Settings");

        final Divider divider = new Divider();
        divider.setId("divider-settings");

        final Label staticLabel = new Label();
        staticLabel.setId("label-static");
        staticLabel.setText("Static");

        final Toggle blobCacheToggle = new Toggle();
        blobCacheToggle.setDefaultValue(settingsConfig.isClientBlobCacheEnabled());
        blobCacheToggle.setId("toggle-blob_cache");
        blobCacheToggle.setText("Client Blob Cache Enabled (requires restart)");
        blobCacheToggle.setTooltip("Defines whether the Client supports Sub Chunk Blob Caching. " +
            "Defaults to true.");

        final Toggle printDebugInfoToggle = new Toggle();
        printDebugInfoToggle.setId("toggle-print_debug_info");
        printDebugInfoToggle.setText("Print Debug Info Enabled (requires restart)");
        printDebugInfoToggle.setDefaultValue(settingsConfig.isPrintDebugInfo());
        printDebugInfoToggle.setTooltip(
            "Defines whether the debug information (resource pack " +
                "content keys, anticheat status and telemetry server version data)" +
                " should be displayed. Defaults to false."
        );

        final Toggle packsToggle = new Toggle();
        packsToggle.setId("toggle-packs");
        packsToggle.setText("Load Debug Resource Packs (requires restart)");
        packsToggle.setDefaultValue(settingsConfig.isLoadPacks());
        packsToggle.setTooltip(
            "If debug packs are enabled or not. The debug screen will not be " +
                "rendered when disabled. Defaults to true."
        );

        final Toggle forceDisableServerAuthBlockBreakingToggle = new Toggle();
        forceDisableServerAuthBlockBreakingToggle.setId(
            "toggle-force_disable_server_auth_block_breaking"
        );
        forceDisableServerAuthBlockBreakingToggle.setText(
            "Force Disable Server Auth Block Breaking (requires restart)"
        );
        forceDisableServerAuthBlockBreakingToggle.setDefaultValue(
            settingsConfig.isForceDisableServerAuthBlockBreaking()
        );
        forceDisableServerAuthBlockBreakingToggle.setTooltip(
            "Whether server auth block breaking is disabled. Defaults to false."
        );

        final Toggle spoofDeviceIDToggle = new Toggle();
        spoofDeviceIDToggle.setId("toggle-spoof_device_id");
        spoofDeviceIDToggle.setText("Should Spoof DeviceId");
        spoofDeviceIDToggle.setDefaultValue(settingsConfig.isShouldSpoofDeviceId());
        spoofDeviceIDToggle.setTooltip(
            "Whether to send an empty DeviceId in the Login Client JWT / Connection Request. " +
                "Defaults to true."
        );

        final Label generalSectionLabel = new Label();
        generalSectionLabel.setId("label-general_section");
        generalSectionLabel.setText("General");

        final Toggle commandsToggle = new Toggle();
        commandsToggle.setId("toggle-commands");
        commandsToggle.setText("Debug Commands Enabled");
        commandsToggle.setDefaultValue(settingsConfig.isDebugCommandsEnabled());
        commandsToggle.setTooltip(
            "Enables debug commands. If this option is disabled, debug commands are not shown " +
                "but can still be used as usual. Defaults to true"
        );

        final Toggle renderBuildInfoToggle = new Toggle();
        renderBuildInfoToggle.setId("toggle-render_build_info");
        renderBuildInfoToggle.setText("Render Build Info");
        renderBuildInfoToggle.setDefaultValue(settingsConfig.isRenderBuildInfo());
        renderBuildInfoToggle.setTooltip("Enables build info rendering. Defaults to true.");

        final Toggle renderExperimentInfoToggle = new Toggle();
        renderExperimentInfoToggle.setId("toggle-render_experiment_info");
        renderExperimentInfoToggle.setText("Render Experiment Info");
        renderExperimentInfoToggle.setDefaultValue(settingsConfig.isRenderExperimentInfo());
        renderExperimentInfoToggle.setTooltip(
            "Enables experiment info rendering. Defaults to true."
        );

        final StepSlider platformTypeSlider = new StepSlider();
        platformTypeSlider.setId("slider-platform_type");
        platformTypeSlider.setText("Platform Type Override");
        platformTypeSlider.addStep(
            Util.CONVERTER.convert(PlatformType.DESKTOP.name()),
            settingsConfig.getPlatformType().equals(PlatformType.DESKTOP)
        );
        platformTypeSlider.addStep(
            Util.CONVERTER.convert(PlatformType.CONSOLE.name()),
            settingsConfig.getPlatformType().equals(PlatformType.CONSOLE)
        );
        platformTypeSlider.addStep(
            Util.CONVERTER.convert(PlatformType.MOBILE.name()),
            settingsConfig.getPlatformType().equals(PlatformType.MOBILE)
        );
        platformTypeSlider.setTooltip(
            "Platform Type Override used for Device Simulation, DeviceOS is sent exclusively in " +
                "the LoginPacket"
        );

        final Input zoneIdOverrideInput = new Input();
        zoneIdOverrideInput.setId("input-zone_id_override");
        zoneIdOverrideInput.setText("Zone ID Override");
        zoneIdOverrideInput.setDefaultText(settingsConfig.getZoneIdOverride());
        zoneIdOverrideInput.setTooltip(
            "Overrides the Zone ID for the Local time displayed in the build info"
        );

        final Label playerDebugRendererLabel = new Label();
        playerDebugRendererLabel.setId("label-player_debug_renderer");
        playerDebugRendererLabel.setText("Player Debug Renderer");

        final Toggle playerDebugRendererToggle = new Toggle();
        playerDebugRendererToggle.setId("toggle-player_debug_renderer");
        playerDebugRendererToggle.setText("Enabled");
        playerDebugRendererToggle.setDefaultValue(settingsConfig.isPlayerDebugRendererEnabled());
        playerDebugRendererToggle.setTooltip(
            "Whether the player debug renderer is enabled. " +
                "Adjust color values below to modify bounding box color. Color values must be between" +
                " 0 and 255. An empty value is considered to be 0. Defaults to true."
        );

        final Input playerDebugRendererColorRInput = this.createColorInput(
            "input-player_debug_renderer_color_r",
            "Color: §cRed",
            settingsConfig.getPlayerDebugRendererColorR()
        );
        final Input playerDebugRendererColorGInput = this.createColorInput(
            "input-player_debug_renderer_color_g",
            "Color: §2Green",
            settingsConfig.getPlayerDebugRendererColorG()
        );
        final Input playerDebugRendererColorBInput = this.createColorInput(
            "input-player_debug_renderer_color_b",
            "Color: §9Blue",
            settingsConfig.getPlayerDebugRendererColorB()
        );

        final Label actorDebugRendererLabel = new Label();
        actorDebugRendererLabel.setId("label-actor_debug_renderer");
        actorDebugRendererLabel.setText("Actor Debug Renderer");

        final Toggle actorDebugRendererToggle = new Toggle();
        actorDebugRendererToggle.setId("toggle-actor_debug_renderer");
        actorDebugRendererToggle.setText("Enabled");
        actorDebugRendererToggle.setDefaultValue(settingsConfig.isActorDebugRendererEnabled());
        actorDebugRendererToggle.setTooltip(
            "Whether the actor debug renderer is enabled. " +
                "Adjust color values below to modify bounding box color. Color values must be between" +
                " 0 and 255. An empty value is considered to be 0. Defaults to true."
        );

        final Toggle actorDebugRendererShowTextToggle = new Toggle();
        actorDebugRendererShowTextToggle.setId("toggle-actor_debug_renderer_show_text");
        actorDebugRendererShowTextToggle.setText("Show Text");
        actorDebugRendererShowTextToggle.setDefaultValue(
            settingsConfig.isActorDebugRendererShowText()
        );
        actorDebugRendererShowTextToggle.setTooltip(
            "Whether to show the actor debug renderer text when actor debug rendering is enbaled."
        );

        final Input actorDebugRendererColorRInput = this.createColorInput(
            "input-actor_debug_renderer_color_r",
            "Color: §cRed",
            settingsConfig.getActorDebugRendererColorR()
        );
        final Input actorDebugRendererColorGInput = this.createColorInput(
            "input-actor_debug_renderer_color_g",
            "Color: §2Green",
            settingsConfig.getActorDebugRendererColorG()
        );
        final Input actorDebugRendererColorBInput = this.createColorInput(
            "input-actor_debug_renderer_color_b",
            "Color: §9Blue",
            settingsConfig.getActorDebugRendererColorB()
        );

        final Label chunkDebugRendererLabel = new Label();
        chunkDebugRendererLabel.setId("label-chunk_debug_renderer");
        chunkDebugRendererLabel.setText("LevelChunk Debug Renderer");

        final Toggle renderCurrentChunkToggle = new Toggle();
        renderCurrentChunkToggle.setId("toggle-render_current_chunk");
        renderCurrentChunkToggle.setText("Enabled");
        renderCurrentChunkToggle.setDefaultValue(settingsConfig.isRenderCurrentChunk());
        renderCurrentChunkToggle.setTooltip(
            "Renders the current chunk and sub chunk based on the client's position. " +
                "Defaults to true."
        );
        final Input chunkRendererColorRInput = this.createColorInput(
            "input-chunk_debug_renderer_color_r",
            "Chunk Render Color: §cRed",
            settingsConfig.getChunkDebugRendererColorR()
        );
        final Input chunkRendererColorGInput = this.createColorInput(
            "input-chunk_debug_renderer_color_g",
            "Chunk Render Color: §2Green",
            settingsConfig.getChunkDebugRendererColorG()
        );
        final Input chunkRendererColorBInput = this.createColorInput(
            "input-chunk_debug_renderer_color_b",
            "Chunk Render Color: §9Blue",
            settingsConfig.getChunkDebugRendererColorB()
        );

        final Label subChunkColorsLabel = new Label();
        subChunkColorsLabel.setId("label-sub_chunk_colors");
        subChunkColorsLabel.setText("SubChunk Colors");

        final Input subChunkRendererColorRInput = this.createColorInput(
            "input-sub_chunk_debug_renderer_color_r",
            "SubChunk Render Color: §cRed",
            settingsConfig.getSubChunkDebugRendererColorR()
        );
        final Input subChunkRendererColorGInput = this.createColorInput(
            "input-sub_chunk_debug_renderer_color_g",
            "SubChunk Render Color: §2Green",
            settingsConfig.getSubChunkDebugRendererColorG()
        );
        final Input subChunkRendererColorBInput = this.createColorInput(
            "input-sub_chunk_debug_renderer_color_b",
            "SubChunk Render Color: §9Blue",
            settingsConfig.getSubChunkDebugRendererColorB()
        );

        final Input cnsScreenMinPacketNumInput = new Input();
        cnsScreenMinPacketNumInput.setId("input-cns_screen_min_packet_num");
        cnsScreenMinPacketNumInput.setText("CNS Screen Min Packet Num");
        cnsScreenMinPacketNumInput.setDefaultText(
            String.valueOf(settingsConfig.getCnsScreenMinPacketNum())
        );
        cnsScreenMinPacketNumInput.setTooltip(
            "Packets whose count is below the specified number will not be displayed on the " +
                "Client Network Stats screen. Defaults to 1 - i.e. all packets are displayed."
        );

        elements.addAll(Arrays.asList(
                header,
                staticLabel,
                blobCacheToggle, printDebugInfoToggle, packsToggle,
                forceDisableServerAuthBlockBreakingToggle,
                divider,
                generalSectionLabel,
                commandsToggle, renderBuildInfoToggle, renderExperimentInfoToggle,
                spoofDeviceIDToggle,
                cnsScreenMinPacketNumInput,
                platformTypeSlider,
                zoneIdOverrideInput,
                divider,
                playerDebugRendererLabel,
                playerDebugRendererToggle, playerDebugRendererColorRInput,
                playerDebugRendererColorGInput, playerDebugRendererColorBInput,
                divider,
                actorDebugRendererLabel,
                actorDebugRendererToggle,
                actorDebugRendererShowTextToggle, actorDebugRendererColorRInput,
                actorDebugRendererColorGInput, actorDebugRendererColorBInput,
                divider,
                chunkDebugRendererLabel,
                renderCurrentChunkToggle,
                chunkRendererColorRInput,
                chunkRendererColorGInput,
                chunkRendererColorBInput,
                subChunkColorsLabel,
                subChunkRendererColorRInput,
                subChunkRendererColorGInput,
                subChunkRendererColorBInput
            )
        );
    }

    private void addDebugSection(TestConfig testConfig, List<Element> elements) {
        final Divider divider = new Divider();
        divider.setId("divider-debug");

        final Header header = new Header();
        header.setId("header-debug");
        header.setText("Developer Settings");

        final Toggle packetTestingToggle = new Toggle();
        packetTestingToggle.setId("toggle-packet_testing");
        packetTestingToggle.setText("§bPacket Testing");
        packetTestingToggle.setDefaultValue(testConfig.isPacketTesting());
        packetTestingToggle.setTooltip("Enables packet testing. Defaults to false");

        final Input debugServerPathInput = new Input();
        debugServerPathInput.setId("input-debug_server_path");
        debugServerPathInput.setText("§bDebug Server Path");
        debugServerPathInput.setTooltip(
            "Set the path to the Bedrock Dedicated Server that is used for debugging purposes."
        );
        debugServerPathInput.setDefaultText(testConfig.getDebugServerPath());

        elements.addAll(
            Arrays.asList(
                divider,
                header,
                packetTestingToggle,
                debugServerPathInput
            )
        );
    }

    private void handleSettingsResponse(CustomResponse response, SettingsConfig settingsConfig,
                                        ConfigRegistry configRegistry, BedrockDebuggerProxy proxy) {
        final boolean blobCache = response.getToggleResponse("toggle-blob_cache");
        final boolean printDebugInfo = response.getToggleResponse("toggle-print_debug_info");
        final boolean commandsEnabled = response.getToggleResponse("toggle-commands");
        final boolean loadPacks = response.getToggleResponse("toggle-packs");
        final boolean playerDebugRendererEnabled = response.getToggleResponse(
            "toggle-player_debug_renderer"
        );
        final boolean actorDebugRendererEnabled = response.getToggleResponse(
            "toggle-actor_debug_renderer"
        );
        final boolean actorDebugRendererShowText = response.getToggleResponse(
            "toggle-actor_debug_renderer_show_text"
        );
        final boolean renderBuildInfo = response.getToggleResponse("toggle-render_build_info");
        final boolean renderExperimentInfo = response.getToggleResponse(
            "toggle-render_experiment_info"
        );
        final boolean renderCurrentChunkToggle = response.getToggleResponse(
            "toggle-render_current_chunk"
        );
        final String cnsScreenMinPacketNumRaw = response.getInputResponse(
            "input-cns_screen_min_packet_num"
        );
        final boolean spoofDeviceIdToggle = response.getToggleResponse(
            "toggle-spoof_device_id"
        );
        final boolean forceDisableServerAuthBlockBreaking = response.getToggleResponse(
            "toggle-force_disable_server_auth_block_breaking"
        );
        final String zoneIdOverride = response.getInputResponse("input-zone_id_override");
        final int platformTypeOrdinal = Integer.parseInt(
            response.getStepSliderResponse("slider-platform_type")
        );

        this.updateToggle(blobCache, settingsConfig.isClientBlobCacheEnabled(),
            settingsConfig::setClientBlobCacheEnabled, proxy, "Client Blob Cache");
        this.updateToggle(printDebugInfo, settingsConfig.isPrintDebugInfo(),
            settingsConfig::setPrintDebugInfo, proxy, "Print Debug Info");
        this.updateToggle(commandsEnabled, settingsConfig.isDebugCommandsEnabled(),
            value -> {
                settingsConfig.setDebugCommandsEnabled(value);
                proxy.getPlayer().sendAvailableCommands(value);
            }, proxy, "Debug Commands");
        this.updateToggle(loadPacks, settingsConfig.isLoadPacks(),
            settingsConfig::setLoadPacks, proxy, "Load Debug Resource Packs");
        this.updateToggle(playerDebugRendererEnabled,
            settingsConfig.isPlayerDebugRendererEnabled(), value -> {
                if (value == settingsConfig.isPlayerDebugRendererEnabled()) {
                    return;
                }
                settingsConfig.setPlayerDebugRendererEnabled(value);
                if (!value) {
                    proxy.getDebugShapeRenderer().clearShapes(
                        shapeId -> shapeId.startsWith("player_")
                    );
                } else {
                    for (final ServerPlayer player : proxy.getPlayers()) {
                        player.renderBounds(settingsConfig);
                    }
                }
            }, proxy, "Player Debug Renderer");
        this.updateToggle(actorDebugRendererEnabled,
            settingsConfig.isActorDebugRendererEnabled(), value -> {
                settingsConfig.setActorDebugRendererEnabled(value);
                if (!value) {
                    proxy.getDebugShapeRenderer().clearShapes(
                        shapeId -> shapeId.startsWith("actor_") ||
                            shapeId.startsWith("circle_")
                    );
                } else {
                    for (final Actor actor : proxy.getActors()) {
                        actor.renderBounds(settingsConfig);
                    }
                }
            }, proxy, "Actor Debug Renderer");
        this.updateToggle(
            actorDebugRendererShowText,
            settingsConfig.isActorDebugRendererShowText(),
            value -> {
                settingsConfig.setActorDebugRendererShowText(value);
                if (!settingsConfig.isActorDebugRendererEnabled()) {
                    return;
                }
                if (!value) {
                    proxy.getDebugShapeRenderer().clearShapes(
                        shapeId -> shapeId.startsWith("actor_box_text_") ||
                            shapeId.startsWith("actor_box_link_text_")
                    );
                } else {
                    for (final Actor actor : proxy.getActors()) {
                        actor.renderBounds(settingsConfig);
                    }
                }
            },
            proxy,
            "Actor Debug Renderer Text"
        );
        this.updateColor(playerDebugRendererEnabled, response, proxy,
            "input-player_debug_renderer_color_r",
            "input-player_debug_renderer_color_g",
            "input-player_debug_renderer_color_b",
            settingsConfig::setPlayerDebugRendererColorR,
            settingsConfig::setPlayerDebugRendererColorG,
            settingsConfig::setPlayerDebugRendererColorB
        );
        this.updateToggle(renderBuildInfo, settingsConfig.isRenderBuildInfo(),
            settingsConfig::setRenderBuildInfo, proxy, "Build Info Rendering");
        this.updateToggle(renderExperimentInfo, settingsConfig.isRenderExperimentInfo(),
            settingsConfig::setRenderExperimentInfo, proxy, "Experiment Info Rendering");
        this.updateToggle(
            renderCurrentChunkToggle,
            settingsConfig.isRenderCurrentChunk(),
            renderCurrentChunk -> {
                settingsConfig.setRenderCurrentChunk(renderCurrentChunk);
                proxy.getPlayer().toggleRenderCurrentChunk(renderCurrentChunk);
            },
            proxy,
            "Current Chunk Rendering"
        );
        this.updateColor(renderCurrentChunkToggle, response, proxy,
            "input-chunk_debug_renderer_color_r",
            "input-chunk_debug_renderer_color_g",
            "input-chunk_debug_renderer_color_b",
            settingsConfig::setChunkDebugRendererColorR,
            settingsConfig::setChunkDebugRendererColorG,
            settingsConfig::setChunkDebugRendererColorB
        );
        this.updateColor(renderCurrentChunkToggle, response, proxy,
            "input-sub_chunk_debug_renderer_color_r",
            "input-sub_chunk_debug_renderer_color_g",
            "input-sub_chunk_debug_renderer_color_b",
            settingsConfig::setSubChunkDebugRendererColorR,
            settingsConfig::setSubChunkDebugRendererColorG,
            settingsConfig::setSubChunkDebugRendererColorB
        );

        try {
            final int cnsScreenMinPacketNum = Integer.parseInt(cnsScreenMinPacketNumRaw);
            if (cnsScreenMinPacketNum <= 0) {
                proxy.getPlayer().sendMessage("§cInvalid number");
            }
            if (settingsConfig.getCnsScreenMinPacketNum() != cnsScreenMinPacketNum) {
                settingsConfig.setCnsScreenMinPacketNum(cnsScreenMinPacketNum);
                proxy.getPlayer().sendMessage("Set CNS Min Packet Num to " + cnsScreenMinPacketNum);
            }
        } catch (NumberFormatException e) {
            proxy.getPlayer().sendMessage("§cInvalid number");
        }

        final PlatformType platformType = PlatformType.from(platformTypeOrdinal);
        if (!settingsConfig.getPlatformType().equals(platformType)) {
            settingsConfig.setPlatformType(platformType);
            proxy.getPlayer().sendMessage("Set Platform Type to " +
                Util.CONVERTER.convert(platformType.name())
            );
        }

        try {
            if (zoneIdOverride == null || zoneIdOverride.isEmpty()) {
                settingsConfig.setZoneIdOverride("");
            } else {
                final ZoneId zoneId = ZoneId.of(zoneIdOverride);
                if (!settingsConfig.getZoneIdOverride().equals(zoneIdOverride)) {
                    proxy.getPlayer().sendMessage("Set Zone ID Override to " + zoneId.getId());
                    settingsConfig.setZoneIdOverride(zoneIdOverride);
                }
            }
        } catch (Throwable e) {
            proxy.getPlayer().sendMessage("§cInvalid Zone ID");
            return;
        }

        this.updateToggle(
            spoofDeviceIdToggle,
            settingsConfig.isShouldSpoofDeviceId(),
            settingsConfig::setShouldSpoofDeviceId,
            proxy,
            "Should Spoof DeviceId"
        );

        this.updateToggle(
            forceDisableServerAuthBlockBreaking,
            settingsConfig.isForceDisableServerAuthBlockBreaking(),
            settingsConfig::setForceDisableServerAuthBlockBreaking,
            proxy,
            "Force Disable Server Auth Block Breaking"
        );

        configRegistry.save(settingsConfig);
    }

    private void handleDebugResponse(CustomResponse response, TestConfig testConfig,
                                     ConfigRegistry configRegistry, BedrockDebuggerProxy proxy) {
        final String debugServerPath = response.getInputResponse("input-debug_server_path");
        final boolean packetTesting = response.getToggleResponse("toggle-packet_testing");

        this.updateToggle(packetTesting, testConfig.isPacketTesting(), testConfig::setPacketTesting,
            proxy, "Packet Testing");

        configRegistry.save(testConfig);

        boolean isInvalidDebugServerPath;
        if (debugServerPath == null || debugServerPath.isEmpty()) {
            isInvalidDebugServerPath = true;
        } else {
            final File file = new File(proxy.getDataFolder(), debugServerPath);
            if (!file.isDirectory()) {
                isInvalidDebugServerPath = true;
            } else {
                isInvalidDebugServerPath = true;
                for (final File f : Objects.requireNonNull(file.listFiles())) {
                    if (f.getName().equalsIgnoreCase("bedrock_server.exe")) {
                        isInvalidDebugServerPath = false;
                        break;
                    }
                }
            }
        }

        if (isInvalidDebugServerPath) {
            if (testConfig.getDebugServerPath() == null) {
                proxy.getPlayer().sendMessage("§cThe provided debug server path is invalid.");
            }
            return;
        } else if (!testConfig.getDebugServerPath().equalsIgnoreCase(debugServerPath)) {
            testConfig.setDebugServerPath(debugServerPath);

            proxy.getPlayer().sendMessage("Set debug server path to: " + debugServerPath);
        }

        configRegistry.save(testConfig);
    }

    private void updateToggle(boolean value, boolean oldValue, Consumer<Boolean> consumer,
                              BedrockDebuggerProxy proxy, String settingName) {
        consumer.accept(value);
        if (value != oldValue) {
            proxy.getPlayer().sendMessage((value ? "Enabled" : "Disabled") + " " + settingName);
        }
    }

    private void updateColor(boolean toggle, CustomResponse response, BedrockDebuggerProxy proxy,
                             String redId, String greenId, String blueId,
                             Consumer<Integer> redC, Consumer<Integer> greenC,
                             Consumer<Integer> blueC) {
        if (toggle) {
            final int red = this.getIntResponse(response.getInputResponse(redId));
            final int green = this.getIntResponse(response.getInputResponse(greenId));
            final int blue = this.getIntResponse(response.getInputResponse(blueId));

            if (!this.isColorInBounds(red) || !this.isColorInBounds(green) ||
                !this.isColorInBounds(blue)) {
                proxy.getPlayer().sendMessage("§cColor not in bounds.");
                return;
            }

            redC.accept(red);
            greenC.accept(green);
            blueC.accept(blue);
        }
    }

    private boolean isColorInBounds(int value) {
        return value >= 0 && value <= 255;
    }

    private Input createColorInput(String id, String text, int value) {
        final Input input = new Input();
        input.setId(id);
        input.setText(text);
        input.setDefaultText(String.valueOf(value));
        return input;
    }

    private int getIntResponse(String response) {
        try {
            return response.isEmpty() ? 0 : Integer.parseInt(response);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}