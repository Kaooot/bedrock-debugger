package dev.kaooot.debugger.server;

import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.config.ConfigRegistry;
import dev.kaooot.debugger.config.MainConfig;
import dev.kaooot.debugger.core.registry.Registries;
import dev.kaooot.debugger.core.registry.RegistryKey;
import dev.kaooot.debugger.network.NetherNetIdentity;
import dev.kaooot.debugger.network.NetworkConstants;
import dev.kaooot.debugger.network.ProxiedPacketHandler;
import dev.kaooot.debugger.util.DebugElement;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.netty.channel.nethernet.NetherNetChannelFactory;
import org.cloudburstmc.netty.channel.nethernet.signaling.NetherNetHTTPServerSignaling;
import org.cloudburstmc.netty.channel.nethernet.signaling.PongData;
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.netty.handler.codec.raknet.server.RakServerRateLimiter;
import org.cloudburstmc.netty.util.nethernet.TokenTrust;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.BedrockPong;
import org.cloudburstmc.protocol.bedrock.nethernet.initializer.BedrockNetherNetChannelInitializer;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.data.EncodingSettings;
import org.cloudburstmc.protocol.bedrock.data.connection.PacketCompressionAlgorithm;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockChannelInitializer;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetTitlePacket;
import org.cloudburstmc.protocol.common.DefinitionRegistry;
import org.cloudburstmc.protocol.common.NamedDefinition;
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class ProxiedServer {

    private final InetSocketAddress address;
    @Getter
    private final BedrockDebuggerProxy proxy;

    @Getter
    private ProxiedPacketHandler packetHandler;

    private ProxiedServerSession session;

    public void start() {
        this.proxy.getLogger().info("Starting proxied server");
        this.packetHandler = new ProxiedPacketHandler(this.proxy, true);

        final MainConfig.TransportType transportType =
            Registries.<ConfigRegistry>getRegistry(RegistryKey.CONFIG)
                .get(MainConfig.class)
                .getTransportType();

        if (transportType == MainConfig.TransportType.NETHERNET) {
            this.startNetherNet();
        } else {
            this.startRakNet();
        }
    }

    private void startRakNet() {
        final BedrockPong pong = new BedrockPong()
            .edition("MCPE")
            .motd("Bedrock Debugger")
            .subMotd("")
            .playerCount(0)
            .maximumPlayerCount(20)
            .gameType("Survival")
            .protocolVersion(NetworkConstants.CODEC.getProtocolVersion())
            .nintendoLimited(false)
            .ipv4Port(this.address.getPort())
            .ipv6Port(this.address.getPort());

        final ServerBootstrap bootstrap = new ServerBootstrap();
        final ChannelFuture channelFuture = bootstrap
            .channelFactory(RakChannelFactory.server(NioDatagramChannel.class))
            .option(RakChannelOption.RAK_ADVERTISEMENT, pong.toByteBuf())
            .group(new NioEventLoopGroup())
            .childHandler(new BedrockChannelInitializer<ProxiedServerSession>() {
                @Override
                protected ProxiedServerSession createSession0(BedrockPeer peer,
                                                              int subClientId) {
                    return ProxiedServer.this.createServerSession(peer, subClientId);
                }

                @Override
                protected void initSession(ProxiedServerSession session) {
                    ProxiedServer.this.initServerSession(session);
                }
            })
            .bind(this.address)
            .awaitUninterruptibly();
        this.handleBindResult(channelFuture);
        channelFuture.channel().pipeline().remove(RakServerRateLimiter.class);
    }

    private void startNetherNet() {
        final PongData pong = new PongData.Builder()
            .setServerName("Bedrock Debugger")
            .setProtocol(NetworkConstants.CODEC.getProtocolVersion())
            .setVersion(NetworkConstants.CODEC.getMinecraftVersion())
            .setLevelName("World")
            .setGameType(0)
            .setPlayerCount(0)
            .setMaxPlayerCount(20)
            .build();

        final NetherNetHTTPServerSignaling signaling;
        try {
            signaling = new NetherNetHTTPServerSignaling.Builder()
                .setIdentity(NetherNetIdentity.get())
                // Serve the /v1/join endpoint ourselves over the bind port.
                .setServeHttp(true)
                // Accept a peer's self-signed assertion; the debugger is not an auth service.
                .setTokenTrust(TokenTrust.ANY)
                // RakNet is not bound in NetherNet mode, so ICE may share the signaling port
                // (iceOnLocalPort defaults to true), and no separate media port is needed.
                .setMotd(pong)
                .build();
        } catch (Exception e) {
            this.proxy.getLogger().error(
                "Failed to configure NetherNet signaling on {}: {}", this.address, e.getMessage()
            );
            System.exit(0);
            return;
        }

        final ServerBootstrap bootstrap = new ServerBootstrap();
        final ChannelFuture channelFuture = bootstrap
            .channelFactory(NetherNetChannelFactory.server(signaling))
            .group(new NioEventLoopGroup())
            .childHandler(new BedrockNetherNetChannelInitializer<ProxiedServerSession>() {
                @Override
                protected ProxiedServerSession createSession0(BedrockPeer peer, int subClientId) {
                    return ProxiedServer.this.createServerSession(peer, subClientId);
                }

                @Override
                protected void initSession(ProxiedServerSession session) {
                    ProxiedServer.this.initServerSession(session);
                }
            })
            .bind(this.address)
            .awaitUninterruptibly();
        this.handleBindResult(channelFuture);
    }

    private ProxiedServerSession createServerSession(BedrockPeer peer, int subClientId) {
        final ProxiedServerSession session = new ProxiedServerSession(
            ProxiedServer.this.proxy, peer, subClientId
        );
        synchronized (ProxiedServer.this) {
            ProxiedServer.this.session = session;
        }
        return session;
    }

    private void initServerSession(ProxiedServerSession session) {
        session.setLogging(true);
        session.setCodec(NetworkConstants.CODEC);
        session.setPacketHandler(ProxiedServer.this.packetHandler);
    }

    private void handleBindResult(ChannelFuture channelFuture) {
        channelFuture.addListener(future -> {
            if (!future.isSuccess()) {
                final Throwable throwable = future.cause();
                if (throwable != null) {
                    this.proxy.getLogger().error(
                        "Failed to start the proxied server on {}: {}",
                        this.address,
                        throwable.getMessage()
                    );
                    System.exit(0);
                }
            } else {
                this.proxy.getLogger().info("Bedrock server started on {}", this.address);
            }
        });
    }

    public void sendPacket(BedrockPacket packet) {
        this.session.sendPacketImmediately(packet);
    }

    public void sendPacketImmediately(BedrockPacket packet) {
        this.session.sendPacketImmediately(packet);
    }

    public boolean isConnected() {
        return this.session != null && this.session.isConnected() && this.session.getPeer() != null;
    }

    public void setCompression(PacketCompressionAlgorithm compression) {
        this.session.setCompression(compression);
    }

    public void close(String reason) {
        if (this.session.isConnected()) {
            this.session.close(reason);
        }
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

    public DefinitionRegistry<NamedDefinition> getCameraPresetDefinitions() {
        return this.session.getPeer().getCodecHelper().getCameraPresetDefinitions();
    }

    public BedrockCodecHelper getCodecHelper() {
        return this.session.getPeer().getCodecHelper();
    }

    public BedrockCodec getCodec() {
        return this.session.getPeer().getCodec();
    }

    public EventLoop getEventLoop() {
        return this.session.getPeer().getChannel().eventLoop();
    }

    public void sendDebugInfos(DebugElement element, List<String> debugInfos) {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(element.getKey());

        for (final String debugInfo : debugInfos) {
            if (debugInfo.isEmpty()) {
                continue;
            }
            stringBuilder.append(debugInfo).append("\n");
        }

        final SetTitlePacket packet = new SetTitlePacket();
        packet.setTitleType(SetTitlePacket.TitleType.SUBTITLE);
        packet.setTitleText(stringBuilder.toString());
        packet.setFadeInTime(-1);
        packet.setStayTime(-1);
        packet.setFadeOutTime(-1);
        packet.setXuid(this.proxy.getPlayer().getXuid());
        packet.setPlatformOnlineId("");
        this.sendPacket(packet);
    }

    public void sendDebugInfo(DebugElement element, String debugInfo) {
        final SetTitlePacket packet = new SetTitlePacket();
        packet.setTitleType(SetTitlePacket.TitleType.SUBTITLE);
        packet.setTitleText(element.getKey() + debugInfo);
        packet.setFadeInTime(-1);
        packet.setStayTime(-1);
        packet.setFadeOutTime(-1);
        packet.setXuid(this.proxy.getPlayer().getXuid());
        packet.setPlatformOnlineId("");
        this.sendPacket(packet);
    }

    public void sendBuildInfo(String buildInfo) {
        final SetTitlePacket packet = new SetTitlePacket();
        packet.setTitleType(SetTitlePacket.TitleType.TITLE);
        packet.setTitleText(DebugElement.BUILD_INFO.getKey() + buildInfo);
        packet.setFadeInTime(-1);
        packet.setStayTime(-1);
        packet.setFadeOutTime(-1);
        packet.setXuid(this.proxy.getPlayer().getXuid());
        packet.setPlatformOnlineId("");
        this.sendPacket(packet);
    }
}