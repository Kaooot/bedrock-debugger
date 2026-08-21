package dev.kaooot.debugger.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.netty.handler.codec.raknet.server.RakServerRateLimiter;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.BedrockPong;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.data.EncodingSettings;
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockChannelInitializer;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetTitlePacket;
import org.cloudburstmc.protocol.common.DefinitionRegistry;
import org.cloudburstmc.protocol.common.NamedDefinition;
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.NetworkConstants;
import dev.kaooot.debugger.network.ProxiedPacketHandler;
import dev.kaooot.debugger.util.DebugElement;

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
        final Channel channel = bootstrap
            .channelFactory(RakChannelFactory.server(NioDatagramChannel.class))
            .option(RakChannelOption.RAK_ADVERTISEMENT, pong.toByteBuf())
            .group(new NioEventLoopGroup())
            .childHandler(new BedrockChannelInitializer<ProxiedServerSession>() {
                @Override
                protected ProxiedServerSession createSession0(BedrockPeer peer,
                                                              int subClientId) {
                    final ProxiedServerSession session = new ProxiedServerSession(
                        ProxiedServer.this.proxy, peer, subClientId
                    );
                    synchronized (ProxiedServer.this) {
                        ProxiedServer.this.session = session;
                    }
                    return session;
                }

                @Override
                protected void initSession(ProxiedServerSession session) {
                    session.setLogging(true);
                    session.setCodec(NetworkConstants.CODEC);
                    session.setPacketHandler(ProxiedServer.this.packetHandler);
                }
            })
            .bind(this.address)
            .syncUninterruptibly()
            .channel();
        channel.pipeline().remove(RakServerRateLimiter.class);

        this.proxy.getLogger().info("Bedrock server started on {}", this.address);
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