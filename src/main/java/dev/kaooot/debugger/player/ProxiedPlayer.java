package dev.kaooot.debugger.player;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.api.auth.util.AuthExtraData;
import dev.kaooot.debugger.command.CommandRegistry;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.level.PlayerChunkManager;
import dev.kaooot.debugger.player.login.LoginData;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.Dimension;
import org.cloudburstmc.protocol.bedrock.data.GameType;
import org.cloudburstmc.protocol.bedrock.data.GraphicsMode;
import org.cloudburstmc.protocol.bedrock.data.TextPacketType;
import org.cloudburstmc.protocol.bedrock.data.command.CommandData;
import org.cloudburstmc.protocol.bedrock.data.definitions.DimensionDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.payload.abilities.SerializedAbilitiesData;
import org.cloudburstmc.protocol.bedrock.data.payload.common.DimensionType;
import org.cloudburstmc.protocol.bedrock.data.payload.diagnostics.MemoryCategory;
import org.cloudburstmc.protocol.bedrock.data.payload.diagnostics.MemoryCategoryCounter;
import org.cloudburstmc.protocol.bedrock.data.payload.pack.PackInstanceId;
import org.cloudburstmc.protocol.bedrock.data.payload.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.data.payload.text.AuthorAndMessage;
import org.cloudburstmc.protocol.bedrock.packet.AvailableCommandsPacket;
import org.cloudburstmc.protocol.bedrock.packet.CraftingDataPacket;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundDiagnosticsPacket;
import org.cloudburstmc.protocol.bedrock.packet.TextPacket;

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

    private ServerPlayer serverPlayer;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private SerializedAbilitiesData serializedAbilitiesData;

    @Getter
    private final PlayerChunkManager playerChunkManager;

    @Getter
    private final CheatClientAuthority cheatClientAuthority;

    @Getter
    @Setter
    private SerializedSkin serializedSkin;

    @Getter
    private final PlayerChunkDebugRenderer chunkDebugRenderer;
    @Getter
    private final PlayerFormManager formManager;

    public ProxiedPlayer(BedrockDebuggerProxy proxy, AuthExtraData authExtraData,
                         LoginData loginData) {
        this.proxy = proxy;
        this.authExtraData = authExtraData;
        this.loginData = loginData;
        this.playerChunkManager = new PlayerChunkManager(proxy, this);
        this.cheatClientAuthority = new CheatClientAuthority(proxy, this);
        this.chunkDebugRenderer = new PlayerChunkDebugRenderer(proxy, this);
        this.formManager = new PlayerFormManager(proxy);
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

    public void updateDimension(DimensionType dimension) {
        this.dimension = dimension;
        this.dimensionDefinition = this.chunkDebugRenderer.getDimensionDefinition(dimension);
    }

    public ServerPlayer asServerPlayer() {
        return this.serverPlayer;
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
}