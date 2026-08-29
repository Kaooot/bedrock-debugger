package dev.kaooot.debugger.player;

import dev.kaooot.debugger.api.shape.DebugText;
import dev.kaooot.debugger.level.LevelChunk;
import dev.kaooot.debugger.level.LevelSubChunk;
import dev.kaooot.debugger.level.block.Block;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.AbilitiesIndex;
import org.cloudburstmc.protocol.bedrock.data.Dimension;
import org.cloudburstmc.protocol.bedrock.data.GameType;
import org.cloudburstmc.protocol.bedrock.data.GeneratorType;
import org.cloudburstmc.protocol.bedrock.data.GraphicsMode;
import org.cloudburstmc.protocol.bedrock.data.TextPacketType;
import org.cloudburstmc.protocol.bedrock.data.command.CommandData;
import org.cloudburstmc.protocol.bedrock.data.definitions.DimensionDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.payload.abilities.SerializedAbilitiesData;
import org.cloudburstmc.protocol.bedrock.data.payload.abilities.SerializedAbilitiesDataSerializedLayer;
import org.cloudburstmc.protocol.bedrock.data.payload.common.DimensionType;
import org.cloudburstmc.protocol.bedrock.data.payload.diagnostics.MemoryCategory;
import org.cloudburstmc.protocol.bedrock.data.payload.diagnostics.MemoryCategoryCounter;
import org.cloudburstmc.protocol.bedrock.data.payload.pack.PackInstanceId;
import org.cloudburstmc.protocol.bedrock.data.payload.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.data.payload.text.AuthorAndMessage;
import org.cloudburstmc.protocol.bedrock.packet.AvailableCommandsPacket;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundCloseFormPacket;
import org.cloudburstmc.protocol.bedrock.packet.CraftingDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.ModalFormResponsePacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerSettingsResponsePacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundDiagnosticsPacket;
import org.cloudburstmc.protocol.bedrock.packet.TextPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.auth.util.AuthExtraData;
import dev.kaooot.debugger.api.forms.BaseForm;
import dev.kaooot.debugger.api.forms.CustomForm;
import dev.kaooot.debugger.api.forms.FormListener;
import dev.kaooot.debugger.api.shape.DebugBox;
import dev.kaooot.debugger.command.CommandRegistry;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.level.PlayerChunkManager;
import dev.kaooot.debugger.player.login.LoginData;
import dev.kaooot.debugger.util.Util;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
public class ProxiedPlayer {

    private final BedrockDebuggerProxy proxy;
    private final AuthExtraData authExtraData;
    @Getter
    private final LoginData loginData;

    @Getter
    @Setter
    private long actorId;
    @Getter
    @Setter
    private long runtimeId;
    @Getter
    @Setter
    private Vector3f rotation = Vector3f.ZERO;
    @Getter
    private final List<CommandData> serverCommands = new ObjectArrayList<>();
    @Getter
    @Setter
    private boolean blockNetworkIdsAreHashes;

    @Getter
    private final List<ItemDefinition> itemDefinitions = new ObjectArrayList<>();
    // dumper data
    @Getter
    @Setter
    private NbtMap itemList;
    @Getter
    @Setter
    private NbtMap itemComponents;
    @Getter
    @Setter
    private NbtMap voxelShapes;
    @Getter
    @Setter
    private NbtMap trimData;
    @Getter
    @Setter
    private CraftingDataPacket craftingData;
    @Getter
    @Setter
    private NbtMap actorInfoList;
    @Getter
    @Setter
    private NbtMap creativeContents;
    @Getter
    @Setter
    private NbtMap cameraPresets;
    @Getter
    @Setter
    private NbtMap cameraAimAssistPresets;
    @Getter
    @Setter
    private NbtMap serverBlockProperties;
    @Getter
    @Setter
    private NbtMap biomeData;
    // dumper data end

    @Getter
    @Setter
    private boolean gammaEnabled;

    private final Int2ObjectMap<BaseForm> forms = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<FormListener> formListeners = new Int2ObjectOpenHashMap<>();
    private int serverSettingsFormId = -1;
    @Getter
    private boolean formOpened;

    @Getter
    @Setter
    private boolean readyToRoll;

    @Getter
    @Setter
    private boolean diagnosticsEnabled;

    @Getter
    private ServerboundDiagnosticsPacket diagnostics;

    @Getter
    @Setter
    private String currentStructureFeature = "";

    @Getter
    @Setter
    private GameType gameType;

    @Getter
    private final List<String> experiments = new ObjectArrayList<>();

    @Getter
    @Setter
    private boolean paused;

    @Getter
    @Setter
    private boolean alwaysDay;

    @Getter
    @Setter
    private int levelTime;

    @Getter
    private final List<DimensionDefinition> dimensionData = new ObjectArrayList<>();

    @Getter
    private DimensionDefinition dimensionDefinition;

    @Getter
    private DimensionType dimension = DimensionType.from(Dimension.OVERWORLD);

    @Getter
    @Setter
    private GraphicsMode graphicsMode;

    @Getter
    private final List<PackInstanceId> packStack = new ObjectArrayList<>();

    @Getter
    @Setter
    private boolean forceDisableVibrantVisuals;

    @Getter
    @Setter
    private boolean resourcePackRequired;

    @Getter
    @Setter
    private boolean stackTexturePackRequired;

    @Getter
    @Setter
    private boolean stackIncludeEditorPacks;

    @Getter
    @Setter
    private String stackBaseGameVersion;

    @Getter
    private final Object2LongMap<MemoryCategory> memoryCategoryValuesCache =
        new Object2LongOpenHashMap<>();

    @Getter
    private boolean enhancedFlightSpeedToggled;
    @Getter
    private final float enhancedFlightSpeedValue = 0.1f;
    @Getter
    private final float defaultWalkSpeed = 0.1f;
    @Getter
    private final float defaultFlySpeed = 0.05f;

    private ServerPlayer serverPlayer;

    protected SerializedAbilitiesData serializedAbilitiesData;

    @Getter
    private final PlayerChunkManager playerChunkManager;

    @Getter
    private final Set<LevelSoundEventPacket> levelSoundEventPackets = new ObjectOpenHashSet<>();

    @Getter
    private final CheatClientAuthority cheatClientAuthority;

    @Getter
    @Setter
    private SerializedSkin serializedSkin;

    @Getter
    private final Map<String, DebugMarkerSettings> customBlockRenderSettings =
        new Object2ObjectOpenHashMap<>();

    public ProxiedPlayer(BedrockDebuggerProxy proxy, AuthExtraData authExtraData,
                         LoginData loginData) {
        this.proxy = proxy;
        this.authExtraData = authExtraData;
        this.loginData = loginData;
        this.playerChunkManager = new PlayerChunkManager(proxy, this);
        this.cheatClientAuthority = new CheatClientAuthority(proxy, this);
    }

    public String getName() {
        return this.authExtraData.getDisplayName();
    }

    public UUID getUniqueId() {
        return this.authExtraData.getIdentity();
    }

    public String getXuid() {
        return this.authExtraData.getXuid();
    }

    public String getTitleId() {
        return this.authExtraData.getTitleId();
    }

    public Vector3f getPosition() {
        return this.asServerPlayer().getPosition();
    }

    public void setPosition(Vector3f position) {
        this.asServerPlayer().setPosition(position);
    }

    public int getChunkX() {
        return this.getPosition().getFloorX() >> 4;
    }

    public int getChunkZ() {
        return this.getPosition().getFloorZ() >> 4;
    }

    public Vector3i getBlockBelow() {
        return Vector3i.from(
            this.getPosition().getFloorX(),
            this.getPosition().getFloorY() - 1,
            this.getPosition().getFloorZ()
        );
    }

    public void sendMessage(String message) {
        final TextPacket packet = new TextPacket();
        packet.setMessageType(TextPacketType.CHAT);
        final AuthorAndMessage body = new AuthorAndMessage();
        body.setMessage(message);
        body.setPlayerName("");
        packet.setBody(body);
        packet.setSendersXUID("");

        this.proxy.getServer().sendPacket(packet);
    }

    public <R> FormListener<R> sendServerSettings(CustomForm form) {
        final int formId = 1337 + this.forms.size();
        this.forms.put(formId, form);

        final ServerSettingsResponsePacket packet = new ServerSettingsResponsePacket();
        packet.setFormID(formId);
        packet.setFormData(((BaseForm<?>) form).toJson().toString());
        this.proxy.getServer().sendPacket(packet);

        this.serverSettingsFormId = formId;
        final FormListener listener = new FormListener();
        this.formListeners.put(formId, listener);
        return listener;
    }

    public <R> FormListener<R> showForm(BaseForm form) {
        if (this.formOpened) {
            return new FormListener<>();
        }
        this.formOpened = true;
        final int formId = 1337 + this.forms.size();
        this.forms.put(formId, form);

        final ModalFormRequestPacket packet = new ModalFormRequestPacket();
        packet.setFormID(formId);
        packet.setFormData(((BaseForm<?>) form).toJson().toString());
        this.proxy.getServer().sendPacket(packet);
        final FormListener listener = new FormListener();
        this.formListeners.put(formId, listener);
        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);
        if (settingsConfig.isPrintDebugInfo()) {
            this.proxy.getLogger().debug("Opened internal form: " + form.getTitle());
        }
        return listener;
    }

    public void parseFormResponse(ModalFormResponsePacket packet) {
        final int formId = packet.getFormID();
        final String formData = packet.getJsonResponse();
        final BaseForm form = this.forms.get(formId);
        if (form != null) {
            final FormListener listener = this.formListeners.get(formId);
            if (formId != this.serverSettingsFormId) {
                this.forms.remove(formId);
                this.formListeners.remove(formId);
            }
            this.formOpened = false;
            if (formData == null || formData.trim().equalsIgnoreCase("null")) {
                listener.getCloseConsumer().accept(null);
            } else {
                final Object response = form.parseResponse(formData);
                if (response == null) {
                    listener.getCloseConsumer().accept(null);
                } else {
                    listener.getResponseConsumer().accept(response);
                }
            }
        }
    }

    public void closeForm() {
        this.proxy.getServer().sendPacket(new ClientboundCloseFormPacket());
        this.forms.clear();
        this.formListeners.clear();
        this.formOpened = false;
    }

    public boolean isValidFormId(int formId) {
        return this.forms.containsKey(formId);
    }

    public void sendAvailableCommands(boolean withInternals) {
        this.sendAvailableCommands(this.serverCommands, withInternals);
    }

    public void sendAvailableCommands(List<CommandData> serverCommands, boolean withInternals) {
        final List<CommandData> commands = new ObjectArrayList<>();

        if (withInternals) {
            final CommandRegistry commandRegistry = Registries.getRegistry(RegistryKey.COMMAND);
            final Set<CommandData> commandDataSet = commandRegistry.getKeys();
            commands.addAll(commandDataSet);

            for (final CommandData command : commands) {
                serverCommands.removeIf(commandData -> commandData.getName()
                    .equalsIgnoreCase(command.getName()));
            }
        }

        commands.addAll(serverCommands);
        commands.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER
            .compare(o1.getName(), o2.getName()));

        final AvailableCommandsPacket pk = new AvailableCommandsPacket();
        pk.getCommands().addAll(commands);

        this.proxy.getServer().sendPacketImmediately(pk);
    }

    public Vector3f getDirectionVector() {
        final float pitch = (float) ((this.rotation.getX() + 90) * Math.PI) / 180f;
        final float yaw = (float) ((this.rotation.getY() + 90) * Math.PI) / 180f;
        final float x = (float) Math.sin(pitch) * (float) Math.cos(yaw);
        final float z = (float) Math.sin(pitch) * (float) Math.sin(yaw);
        final float y = (float) Math.cos(pitch);
        return Vector3f.from(x, y, z).normalize();
    }

    public String getDirection() {
        float rotation = this.getRotation().getY() % 360;
        if (rotation < 0) {
            rotation += 360.0f;
        }
        if (45 <= rotation && rotation < 135) {
            return "west";
        } else if (135 <= rotation && rotation < 225) {
            return "north";
        } else if (225 <= rotation && rotation < 315) {
            return "east";
        } else {
            return "south";
        }
    }

    public ServerPlayer asServerPlayer() {
        return this.serverPlayer;
    }

    private DimensionDefinition getDimensionDefinition(DimensionType dimension) {
        DimensionDefinition target = null;
        for (final DimensionDefinition group : this.dimensionData) {
            if (group.getDimensionType().equals(dimension)) {
                target = group;
                break;
            }
        }
        if (target == null) {
            var data = DimensionHeightFallbackData.valueOf(dimension.asEnum().name());
            target = new DimensionDefinition(
                dimension.asEnum().name(),
                data.getMaxHeight(),
                data.getMinHeight(),
                GeneratorType.UNDEFINED,
                dimension,
                new UUID(0L, 0L),
                "minecraft:ocean"
            );
        }
        return target;
    }

    private static final String CHUNK_POS_RENDER_SHAPE_ID = "box-chunk_outline";
    private static final String SUB_CHUNK_POS_RENDER_SHAPE_ID = "box-sub_chunk_outline";
    private static final int CHUNK_WIDTH = 16;
    private static final int CHUNK_WIDTH_HALF = CHUNK_WIDTH / 2;
    private static final int SUB_CHUNK_HEIGHT = 16;

    public void toggleRenderCurrentChunk(boolean newToggleValue) {
        if (this.dimension.asEnum().equals(Dimension.UNDEFINED)) {
            this.proxy.getLogger().warn(
                "Failed to toggle Render Current Chunk setting because the dimension data " +
                    "for the given dimension has not been found"
            );
            return;
        }
        final DimensionDefinition target = this.dimensionDefinition;
        if (newToggleValue) {
            final DebugBox chunkOutlineBox = this.createChunkRenderDebugBox(
                target.getHeightMinimum(), target.getHeightMaximum(), false
            );
            final DebugBox subChunkOutlineBox = this.createChunkRenderDebugBox(0, 0, true);

            this.proxy.getDebugShapeRenderer().renderShapes(chunkOutlineBox, subChunkOutlineBox);
        } else {
            if (this.proxy.getDebugShapeRenderer().isShapeRendered(CHUNK_POS_RENDER_SHAPE_ID)) {
                this.proxy.getDebugShapeRenderer().removeShape(CHUNK_POS_RENDER_SHAPE_ID);
            }
            if (this.proxy.getDebugShapeRenderer().isShapeRendered(SUB_CHUNK_POS_RENDER_SHAPE_ID)) {
                this.proxy.getDebugShapeRenderer().removeShape(SUB_CHUNK_POS_RENDER_SHAPE_ID);
            }
        }
    }

    public void updateChunkPosForRenderingIfEnabled(SettingsConfig config) {
        if (!config.isRenderCurrentChunk() || this.dimension.asEnum().equals(Dimension.UNDEFINED)) {
            return;
        }
        final DimensionDefinition target = this.dimensionDefinition;
        DebugBox chunkOutlineBox = this.proxy.getDebugShapeRenderer()
            .getShape(CHUNK_POS_RENDER_SHAPE_ID, DebugBox.class);
        DebugBox subChunkOutlineBox = this.proxy.getDebugShapeRenderer()
            .getShape(SUB_CHUNK_POS_RENDER_SHAPE_ID, DebugBox.class);
        if (chunkOutlineBox == null || subChunkOutlineBox == null) {
            chunkOutlineBox = this.createChunkRenderDebugBox(
                target.getHeightMinimum(), target.getHeightMaximum(), false
            );
            subChunkOutlineBox = this.createChunkRenderDebugBox(0, 0, true);
        } else {
            final Vector3f location = chunkOutlineBox.getLocation();
            final boolean chunkChange = this.getChunkX() != (location.getFloorX() >> 4) ||
                this.getChunkZ() != (location.getFloorZ() >> 4);
            if (chunkChange) {
                chunkOutlineBox.setLocation(
                    Vector3f.from(
                        (this.getChunkX() << 4) + CHUNK_WIDTH_HALF,
                        (target.getHeightMaximum() + target.getHeightMinimum()) / 2f,
                        (this.getChunkZ() << 4) + CHUNK_WIDTH_HALF
                    )
                );
            }
            if (chunkChange || this.getPosition().getFloorY() >> 4 != (location.getFloorY() >> 4)) {
                subChunkOutlineBox.setLocation(
                    Vector3f.from(
                        (this.getChunkX() << 4) + CHUNK_WIDTH_HALF,
                        (this.ensureBoundsChunkY(target.getHeightMinimum(),
                            target.getHeightMaximum()) *
                            SUB_CHUNK_HEIGHT) + (SUB_CHUNK_HEIGHT / 2f),
                        (this.getChunkZ() << 4) + CHUNK_WIDTH_HALF
                    )
                );
            }
        }
        this.proxy.getDebugShapeRenderer().renderShapes(chunkOutlineBox, subChunkOutlineBox);
    }

    public void setDiagnostics(ServerboundDiagnosticsPacket packet) {
        this.diagnostics = packet;
        for (final MemoryCategoryCounter memoryCategoryValue : packet.getMemoryCategoryValues()) {
            this.memoryCategoryValuesCache.put(
                memoryCategoryValue.getCategory(),
                memoryCategoryValue.getCurrentBytes()
            );
        }
    }

    public void toggleEnhancedFlySpeed() {
        this.enhancedFlightSpeedToggled = !this.enhancedFlightSpeedToggled;

        this.sendMessage(
            (this.enhancedFlightSpeedToggled ? "Enabled" : "Disabled") + " Enhanced Fly Speed"
        );

        final UpdateAbilitiesPacket updateAbilitiesPacket = new UpdateAbilitiesPacket();
        final SerializedAbilitiesData data = this.serializedAbilitiesData;
        for (final SerializedAbilitiesDataSerializedLayer layer : data.getLayers()) {
            layer.getAbilityValues().add(AbilitiesIndex.FLYING);
            layer.setFlySpeed(
                this.enhancedFlightSpeedToggled ?
                    this.enhancedFlightSpeedValue : this.defaultFlySpeed
            );
        }
        updateAbilitiesPacket.setData(data);
        this.proxy.getServer().sendPacket(updateAbilitiesPacket);
    }

    public void handleEnhancedFlySpeedAbilities(UpdateAbilitiesPacket packet) {
        if (packet.getData().getTargetPlayerRawId() != this.actorId) {
            return;
        }
        this.serializedAbilitiesData = packet.getData();
        if (!this.enhancedFlightSpeedToggled) {
            return;
        }
        final SerializedAbilitiesData data = packet.getData();
        for (final SerializedAbilitiesDataSerializedLayer layer : data.getLayers()) {
            layer.setFlySpeed(this.enhancedFlightSpeedValue);
        }
    }

    public void updateDimension(DimensionType dimension) {
        this.dimension = dimension;
        this.dimensionDefinition = this.getDimensionDefinition(dimension);
    }

    private DebugBox createChunkRenderDebugBox(int heightMax, int heightMin, boolean subChunk) {
        final SettingsConfig settingsConfig = Registries.
            <ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(SettingsConfig.class);

        final DebugBox box = new DebugBox();
        box.setId(subChunk ? SUB_CHUNK_POS_RENDER_SHAPE_ID : CHUNK_POS_RENDER_SHAPE_ID);
        box.setLocation(
            Vector3f.from(
                (this.getChunkX() << 4) + CHUNK_WIDTH_HALF,
                subChunk ?
                    (this.ensureBoundsChunkY(heightMin, heightMax) * SUB_CHUNK_HEIGHT) +
                        (SUB_CHUNK_HEIGHT / 2f) :
                    (Math.abs(heightMin) + Math.abs(heightMax)) / 2f,
                (this.getChunkZ() << 4) + CHUNK_WIDTH_HALF
            )
        );
        box.setBoxBound(
            Vector3f.from(
                CHUNK_WIDTH - (subChunk ? 0.01f : 0),
                subChunk ? SUB_CHUNK_HEIGHT : Math.abs(heightMax) + Math.abs(heightMin),
                CHUNK_WIDTH - (subChunk ? 0.01f : 0))
        );
        box.setColor(subChunk ? Util.rgbToColor(
                settingsConfig.getSubChunkDebugRendererColorR(),
                settingsConfig.getSubChunkDebugRendererColorG(),
                settingsConfig.getSubChunkDebugRendererColorB()
            ) : Util.rgbToColor(
                settingsConfig.getChunkDebugRendererColorR(),
                settingsConfig.getChunkDebugRendererColorG(),
                settingsConfig.getChunkDebugRendererColorB()
            )
        );
        return box;
    }

    public void updateCustomBlockDebugMarkers(LevelChunk chunk) {
        for (final LevelSubChunk subChunk : chunk.getSubChunks()) {
            subChunk.forEachBlock(0, (localX, localY, localZ, block) -> {
                if (block.getState() == null) {
                    return;
                }
                final String name = block.getState().getString("name");
                final int blockX = (chunk.getX() << 4) + localX;
                final int blockY = (subChunk.getIndex() << 4) + localY;
                final int blockZ = (chunk.getZ() << 4) + localZ;
                if (this.customBlockRenderSettings.keySet().stream()
                    .noneMatch(id -> id.equalsIgnoreCase(name))) {
                    return;
                }
                this.updateCustomBlockDebugMarker(block, blockX, blockY, blockZ);
            });
        }
    }

    public void updateCustomBlockDebugMarker(Block block, int blockX, int blockY, int blockZ) {
        for (final String id : this.customBlockRenderSettings.keySet()) {
            if (block.getState().getString("name").equals(id)) {
                final DebugMarkerSettings settings = this.customBlockRenderSettings.get(id);
                final String textId = "debug_marker_" + id + "_" +
                    blockX + "," + blockY + "," + blockZ;

                final DebugText text = new DebugText();
                text.setId(textId);
                text.setText(id.split(":")[1]);
                text.setLocation(Vector3f.from(blockX + 0.5f, blockY + 1, blockZ + 0.5f));
                text.setDimension(this.proxy.getPlayer().getDimension());
                text.setColor(settings.getTextColor());
                text.setBackgroundColor(settings.getTextBackgroundColor());
                text.setScale(1.5f);

                this.proxy.getDebugShapeRenderer().renderShape(text);
                break;
            }
        }
    }

    @Getter
    @RequiredArgsConstructor
    private enum DimensionHeightFallbackData {
        OVERWORLD(-64, 320),
        NETHER(0, 128),
        THE_END(0, 256);

        private final int minHeight;
        private final int maxHeight;
    }

    private int ensureBoundsChunkY(int heightMin, int heightMax) {
        return (Math.min(Math.max(this.getBlockBelow().getY(), heightMin),
            heightMax - 1) >> 4);
    }
}