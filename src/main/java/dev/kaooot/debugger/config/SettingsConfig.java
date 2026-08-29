package dev.kaooot.debugger.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import dev.kaooot.debugger.api.config.Config;
import org.cloudburstmc.protocol.bedrock.data.PlatformType;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SettingsConfig extends Config {

    @SerializedName("client_blob_cache_enabled")
    private boolean clientBlobCacheEnabled = true;
    @SerializedName("print_debug_info")
    private boolean printDebugInfo;
    @SerializedName("debug_commands_enabled")
    private boolean debugCommandsEnabled = true;
    @SerializedName("load_packs")
    private boolean loadPacks = true;
    @SerializedName("player_debug_renderer_enabled")
    private boolean playerDebugRendererEnabled = true;
    @SerializedName("player_debug_renderer_color_r")
    private int playerDebugRendererColorR = 170;
    @SerializedName("player_debug_renderer_color_g")
    private int playerDebugRendererColorG;
    @SerializedName("player_debug_renderer_color_b")
    private int playerDebugRendererColorB;
    @SerializedName("actor_debug_renderer_enabled")
    private boolean actorDebugRendererEnabled = true;
    @SerializedName("actor_debug_renderer_show_text")
    private boolean actorDebugRendererShowText = true;
    @SerializedName("actor_debug_renderer_color_r")
    private int actorDebugRendererColorR = 170;
    @SerializedName("actor_debug_renderer_color_g")
    private int actorDebugRendererColorG;
    @SerializedName("actor_debug_renderer_color_b")
    private int actorDebugRendererColorB;
    @SerializedName("render_build_info")
    private boolean renderBuildInfo = true;
    @SerializedName("render_experiment_info")
    private boolean renderExperimentInfo = true;
    @SerializedName("render_current_chunk")
    private boolean renderCurrentChunk = true;
    @SerializedName("chunk_debug_renderer_color_r")
    private int chunkDebugRendererColorR = 255;
    @SerializedName("chunk_debug_renderer_color_g")
    private int chunkDebugRendererColorG = 255;
    @SerializedName("chunk_debug_renderer_color_b")
    private int chunkDebugRendererColorB = 85;
    @SerializedName("sub_chunk_debug_renderer_color_r")
    private int subChunkDebugRendererColorR = 85;
    @SerializedName("sub_chunk_debug_renderer_color_g")
    private int subChunkDebugRendererColorG = 255;
    @SerializedName("sub_chunk_debug_renderer_color_b")
    private int subChunkDebugRendererColorB = 85;
    @SerializedName("cns_screen_min_packet_num")
    private int cnsScreenMinPacketNum = 1;
    @SerializedName("override_client_network_version")
    private boolean overrideClientNetworkVersion;
    @SerializedName("should_spoof_device_id")
    private boolean shouldSpoofDeviceId = true;
    @SerializedName("force_enable_persona_skins")
    private boolean forceEnablePersonaSkins;
    @SerializedName("force_disable_server_auth_block_breaking")
    private boolean forceDisableServerAuthBlockBreaking;
    @SerializedName("zone_id_override")
    private String zoneIdOverride = "";
    @SerializedName("platform_type")
    private PlatformType platformType = PlatformType.MOBILE;

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public Config getDefaults() {
        return this;
    }
}