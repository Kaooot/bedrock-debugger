package dev.kaooot.debugger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.kaooot.debugger.actor.Actor;
import dev.kaooot.debugger.api.auth.util.MsaAuth;
import dev.kaooot.debugger.api.logging.Logger;
import dev.kaooot.debugger.api.scheduler.TaskScheduler;
import dev.kaooot.debugger.api.shape.DebugShapeRenderer;
import dev.kaooot.debugger.client.ProxiedClient;
import dev.kaooot.debugger.config.AccountsConfig;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.MainConfig;
import dev.kaooot.debugger.config.SettingsConfig;
import dev.kaooot.debugger.core.model.gatherings.JoinExperienceResponse;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.core.service.AuthServiceConnection;
import dev.kaooot.debugger.core.service.GatheringServiceConnection;
import dev.kaooot.debugger.imgui.ImGuiAdapter;
import dev.kaooot.debugger.input.KeyInputListener;
import dev.kaooot.debugger.logging.MainLogger;
import dev.kaooot.debugger.network.NetworkConstants;
import dev.kaooot.debugger.network.log.PacketLog;
import dev.kaooot.debugger.pack.PackManager;
import dev.kaooot.debugger.player.ProxiedPlayer;
import dev.kaooot.debugger.player.ServerPlayer;
import dev.kaooot.debugger.server.ProxiedServer;
import dev.kaooot.debugger.util.BedrockGameVersion;
import dev.kaooot.debugger.util.BlockPaletteGenerator;
import dev.kaooot.debugger.util.BlockPaletteManager;
import dev.kaooot.debugger.util.DebugHttpServer;
import dev.kaooot.debugger.util.DebugScreenInfo;
import dev.kaooot.debugger.util.RuntimeBlockDefinitionRegistry;
import dev.kaooot.debugger.util.protocoldocs.ProtocolDocsParser;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.File;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils;
import org.cloudburstmc.protocol.common.util.Preconditions;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@Getter
public class BedrockDebuggerProxy {

    private final Logger logger = new MainLogger();
    private final MsaAuth msaAuth;
    private final Gson gson = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();
    private final File dataFolder;
    private final File dataLogsFolder;
    private final PackManager packManager;
    private final KeyPair keyPair;
    private final boolean loadPacks;
    private final TaskScheduler scheduler = new TaskScheduler();
    private final RuntimeBlockDefinitionRegistry blockDefinitionRegistry =
        new RuntimeBlockDefinitionRegistry();

    private ProxiedClient client;
    private final ProxiedServer server;

    @Setter
    private ProxiedPlayer player;
    private final DebugScreenInfo debugScreenInfo;
    private final BlockPaletteManager blockPaletteManager;
    private final KeyInputListener keyInputListener;

    private final List<ServerPlayer> players = new ObjectArrayList<>();
    private final List<Actor> actors = new ObjectArrayList<>();

    private final AuthServiceConnection authServiceConnection;
    private final GatheringServiceConnection gatheringServiceConnection;
    private final DebugShapeRenderer debugShapeRenderer;

    private String remoteAddress;
    private int remotePort;

    @Setter
    private boolean transferring;

    private final ImGuiAdapter imGuiAdapter;
    private final PacketLog packetLog = new PacketLog(this);

    private final DebugHttpServer debugHttpServer;
    private final BlockPaletteGenerator blockPaletteGenerator;
    private final ProtocolDocsParser protocolDocsParser;

    public BedrockDebuggerProxy() {
        System.setProperty("bedrock.maxDecompressedBytes", String.valueOf(Integer.MAX_VALUE));
//        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

        this.getLogger().info(
            "Starting bedrock-debugger v" +
                NetworkConstants.CODEC.getMinecraftVersion() +
                " (" + NetworkConstants.CODEC.getProtocolVersion() + ")"
        );
        this.dataFolder = new File("data");
        this.dataLogsFolder = new File("data/logs/");

        if (!this.dataLogsFolder.exists()) {
            this.dataLogsFolder.mkdirs();
        }

        this.msaAuth = new MsaAuth(this);

        Registries.init();

        final ConfigRegistry configRegistry = Registries.getRegistry(RegistryKey.CONFIG);
        final MainConfig config = configRegistry.get(MainConfig.class);
        final AccountsConfig accountsConfig = configRegistry.get(AccountsConfig.class);

        this.msaAuth.doPrompt(accountsConfig, config);

        this.keyPair = EncryptionUtils.createKeyPair();
        this.loadPacks = configRegistry.get(SettingsConfig.class).isLoadPacks();
        this.packManager = new PackManager();
        if (this.loadPacks) {
            this.packManager.loadPacks(this);
        } else {
            this.logger.info(
                "Skipped loading packs (Load Debug Resource Packs toggle is disabled)"
            );
        }

        this.debugScreenInfo = new DebugScreenInfo(this);

        this.blockPaletteManager = new BlockPaletteManager(this);
        this.blockPaletteManager.loadBlockPalette();

        this.keyInputListener = new KeyInputListener(this);
        this.keyInputListener.init();

        this.authServiceConnection = new AuthServiceConnection(this);
        this.gatheringServiceConnection = new GatheringServiceConnection(this);

        this.imGuiAdapter = new ImGuiAdapter(this);
        this.debugHttpServer = new DebugHttpServer(this);
        this.debugHttpServer.start();

        this.blockPaletteGenerator = new BlockPaletteGenerator(this);
        this.protocolDocsParser = new ProtocolDocsParser();

        this.connect(config);
        this.server = new ProxiedServer(new InetSocketAddress(config.getProxyAddress(),
            config.getProxyPort()), this);
        this.server.start();
        this.debugShapeRenderer = new DebugShapeRenderer(this.server);
        this.shutdownIfDisconnected();
        this.debugHttpServer.stop();
        this.imGuiAdapter.stop();
        System.exit(0);
    }

    public void connect(String address, int port) {
        this.remoteAddress = address;
        this.remotePort = port;
        final ProxiedClient client = new ProxiedClient(new InetSocketAddress(address, port), this);
        try {
            client.start();
            this.client = client;
        } catch (Exception e) {
            this.logger.error("Could not connect to the remote server: " + e.getMessage());
            System.exit(0);
        }
    }

    public void connect(MainConfig config) {
        final String remoteAddress;
        final int remotePort;
        if (config.getConnectionType().equals(MainConfig.ConnectionType.DEFAULT)) {
            remoteAddress = config.getRemoteAddress();
            remotePort = config.getRemotePort();
        } else {
            final String experienceId = config.getExperienceId();
            Preconditions.checkArgument(experienceId != null && !experienceId.isEmpty(),
                "The experience id must be valid");

            final BedrockGameVersion version = BedrockGameVersion.from(
                NetworkConstants.CODEC.getMinecraftVersion()
            );
            this.authServiceConnection.startSession(version, false);

            final JoinExperienceResponse.Result result = this.gatheringServiceConnection
                .joinExperience(experienceId).getResult();
            remoteAddress = result.getIpV4Address();
            remotePort = result.getPort();
            this.logger.debug("Join experience result: {}", result);
        }
        this.connect(remoteAddress, remotePort);
    }

    private void shutdownIfDisconnected() {
        while (true) {
            try {
                if ((!this.client.isConnected() ||
                    (this.player != null && !this.server.isConnected())) && !this.transferring) {
                    break;
                }

                synchronized (this) {
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}