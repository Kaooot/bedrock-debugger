package dev.kaooot.debugger.client;

import dev.kaooot.debugger.api.auth.util.AuthExtraData;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;
import java.security.interfaces.ECPublicKey;
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.netty.channel.nethernet.NetherNetChannelFactory;
import org.cloudburstmc.netty.channel.nethernet.config.NetherChannelOption;
import org.cloudburstmc.netty.channel.nethernet.signaling.HttpSignalingSettings;
import org.cloudburstmc.netty.channel.nethernet.signaling.NetherNetHTTPClientSignaling;
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelMetrics;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.netty.util.nethernet.OperatorIdentity;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.data.EncodingSettings;
import org.cloudburstmc.protocol.bedrock.data.connection.PacketCompressionAlgorithm;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.nethernet.initializer.BedrockNetherNetChannelInitializer;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockChannelInitializer;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.common.DefinitionRegistry;
import org.cloudburstmc.protocol.common.NamedDefinition;
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.MainConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.network.NetherNetIdentity;
import dev.kaooot.debugger.network.NetworkConstants;
import dev.kaooot.debugger.network.ProxiedPacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class ProxiedClient {

    @Getter
    private final InetSocketAddress serverAddress;
    private final BedrockDebuggerProxy proxy;

    @Getter
    private ProxiedPacketHandler packetHandler;

    private ProxiedClientSession session;

    @Getter
    private int receivedCountAvg;
    @Getter
    private int sentCountAvg;

    private int receivedCount;
    private int sentCount;

    @Getter
    private MainConfig.TransportType transportType = MainConfig.TransportType.RAKNET;

    public void start() {
        this.proxy.getLogger().info("Starting proxied client");
        this.packetHandler = new ProxiedPacketHandler(this.proxy, false);
        this.proxy.getScheduler().schedule(() -> {
            ProxiedClient.this.receivedCountAvg = receivedCount;
            ProxiedClient.this.sentCountAvg = sentCount;
            ProxiedClient.this.sentCount = 0;
            ProxiedClient.this.receivedCount = 0;
        }, 20);

        this.transportType = Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG)
            .get(MainConfig.class)
            .getTransportType();

        if (this.transportType == MainConfig.TransportType.NETHERNET) {
            final AuthExtraData identity = AuthExtraData.fromChain(
                this.proxy.getMsaAuth().generateLoginChain(
                    (ECPublicKey) this.proxy.getKeyPair().getPublic(),
                    NetworkConstants.CODEC.getMinecraftVersion()
                )
            );
            this.connectNetherNet(identity.getXuid(), identity.getDisplayName());
            return;
        }

        this.connectRakNet();
    }

    private void connectRakNet() {
        final RakChannelMetrics metrics = new RakChannelMetrics() {
            @Override
            public void bytesIn(int count) {
                ProxiedClient.this.receivedCount += count;
            }

            @Override
            public void bytesOut(int count) {
                ProxiedClient.this.sentCount += count;
            }
        };

        final NioEventLoopGroup group = new NioEventLoopGroup();
        final long clientGuid = ThreadLocalRandom.current().nextLong();
        final ChannelFuture channelFuture = new Bootstrap()
            .channelFactory(RakChannelFactory.client(NioDatagramChannel.class))
            .option(
                RakChannelOption.RAK_PROTOCOL_VERSION,
                NetworkConstants.CODEC.getRaknetProtocolVersion()
            )
            .option(
                RakChannelOption.RAK_TIME_BETWEEN_SEND_CONNECTION_ATTEMPTS_MS, 200
            )
            .option(RakChannelOption.RAK_GUID, clientGuid)
            .option(RakChannelOption.RAK_METRICS, metrics)
            .group(group)
            .handler(new BedrockChannelInitializer<ProxiedClientSession>() {
                @Override
                protected ProxiedClientSession createSession0(BedrockPeer peer,
                                                              int subClientId) {
                    final ProxiedClientSession session = new ProxiedClientSession(
                        ProxiedClient.this.proxy, peer, subClientId
                    );
                    synchronized (ProxiedClient.this) {
                        ProxiedClient.this.session = session;
                    }
                    return session;
                }

                @Override
                protected void initSession(ProxiedClientSession session) {
                    session.setLogging(true);
                    session.setCodec(NetworkConstants.CODEC);
                    session.setPacketHandler(ProxiedClient.this.packetHandler);
                }
            })
            .connect(this.serverAddress, new InetSocketAddress("0.0.0.0", 0))
            .awaitUninterruptibly();

        if (channelFuture.isSuccess()) {
            this.proxy.getLogger().info(
                "Bedrock client connected to {}",
                this.serverAddress.toString()
            );
            return;
        }

        final Throwable cause = channelFuture.cause();
        group.shutdownGracefully();

        this.proxy.getLogger().error(
            "Could not connect to the remote server: {}",
            cause == null ? "unknown reason" : cause.getMessage()
        );
        System.exit(0);
    }

    public void connectNetherNet(String xuid, String name) {
        final OperatorIdentity identity;
        try {
            identity = NetherNetIdentity.get().forPlayer(xuid, name);
        } catch (Exception e) {
            this.proxy.getLogger().error("Failed to build the NetherNet identity assertion", e);
            System.exit(0);
            return;
        }

        final HttpSignalingSettings signalingSettings =
            HttpSignalingSettings.DEFAULT.withScheme(HttpSignalingSettings.Scheme.HTTP);

        final NioEventLoopGroup group = new NioEventLoopGroup();
        final ChannelFuture channelFuture = new Bootstrap()
            .group(group)
            .channelFactory(NetherNetChannelFactory.client(
                () -> new NetherNetHTTPClientSignaling(signalingSettings)
            ))
            .option(NetherChannelOption.NETHER_CLIENT_HANDSHAKE_TIMEOUT_MS, 30000)
            .option(NetherChannelOption.NETHER_CLIENT_IDENTITY, identity)
            .handler(new BedrockNetherNetChannelInitializer<ProxiedClientSession>() {
                @Override
                protected ProxiedClientSession createSession0(BedrockPeer peer, int subClientId) {
                    final ProxiedClientSession session = new ProxiedClientSession(
                        ProxiedClient.this.proxy, peer, subClientId
                    );
                    synchronized (ProxiedClient.this) {
                        ProxiedClient.this.session = session;
                    }
                    return session;
                }

                @Override
                protected void initSession(ProxiedClientSession session) {
                    session.setLogging(true);
                    session.setCodec(NetworkConstants.CODEC);
                    session.setPacketHandler(ProxiedClient.this.packetHandler);
                }
            })
            .connect(this.serverAddress)
            .awaitUninterruptibly();

        if (channelFuture.isSuccess()) {
            this.proxy.getLogger().info(
                "Bedrock client connected to {} over NetherNet",
                this.serverAddress.toString()
            );
            return;
        }

        final Throwable cause = channelFuture.cause();
        group.shutdownGracefully();

        this.proxy.getLogger().error(
            "Could not connect to the remote server over NetherNet: {}",
            cause == null ? "unknown reason" : cause.getMessage()
        );
        System.exit(0);
    }

    public void sendPacket(BedrockPacket packet) {
        this.session.sendPacketImmediately(packet);
    }

    public void sendPacketImmediately(BedrockPacket packet) {
        this.session.sendPacketImmediately(packet);
    }

    public boolean isConnected() {
        return this.session != null && this.session.isConnected() &&
            this.session.getPeer() != null;
    }

    public void setCompression(PacketCompressionAlgorithm compression) {
        this.session.setCompression(compression);
    }

    public void enableEncryption(SecretKey key) {
        if (this.transportType == MainConfig.TransportType.NETHERNET) {
            this.proxy.getLogger().warn(
                "Ignoring Bedrock encryption handshake over NetherNet (transport is already encrypted)"
            );
            return;
        }
        this.session.enableEncryption(key);
    }

    public void close(String reason) {
        if (this.session.isConnected()) {
            this.session.close(reason);
        }
    }

    public void disconnect(String reason) {
        final ProxiedClientSession session = this.session;
        if (session == null || !session.isConnected()) {
            return;
        }
        session.close(reason);
        session.getPeer().getChannel().closeFuture().awaitUninterruptibly(500);
    }

    public void setItemDefinitions(DefinitionRegistry<ItemDefinition> registry) {
        this.session.getPeer().getCodecHelper().setItemDefinitions(registry);
    }

    public SimpleDefinitionRegistry<ItemDefinition> getItemDefinitions() {
        return (SimpleDefinitionRegistry<ItemDefinition>) this.session.getPeer().getCodecHelper()
            .getItemDefinitions();
    }

    public void setBlockDefinitions(DefinitionRegistry<BlockDefinition> registry) {
        this.session.getPeer().getCodecHelper().setBlockDefinitions(registry);
    }

    public void setEncodingSettings(EncodingSettings encodingSettings) {
        this.session.getPeer().getCodecHelper().setEncodingSettings(encodingSettings);
    }

    public void setCameraPresetDefinitions(DefinitionRegistry<NamedDefinition> registry) {
        this.session.getPeer().getCodecHelper().setCameraPresetDefinitions(registry);
    }

    public EventLoop getEventLoop() {
        return this.session.getPeer().getChannel().eventLoop();
    }

    public BedrockCodecHelper getCodecHelper() {
        return this.session.getPeer().getCodecHelper();
    }

    public BedrockCodec getCodec() {
        return this.session.getPeer().getCodec();
    }
}