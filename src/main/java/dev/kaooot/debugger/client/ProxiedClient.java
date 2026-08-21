package dev.kaooot.debugger.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelMetrics;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.data.EncodingSettings;
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockChannelInitializer;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.common.DefinitionRegistry;
import org.cloudburstmc.protocol.common.NamedDefinition;
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry;
import dev.kaooot.debugger.BedrockDebuggerProxy;
import dev.kaooot.debugger.network.NetworkConstants;
import dev.kaooot.debugger.network.ProxiedPacketHandler;

/**
 * Copyright (c) Kaooot. All rights reserved.
 *
 * @author Kaooot
 */
@RequiredArgsConstructor
public class ProxiedClient {

    private final InetSocketAddress address = new InetSocketAddress("0.0.0.0", 0);
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

    public void start() {
        this.proxy.getLogger().info("Starting proxied client");
        this.packetHandler = new ProxiedPacketHandler(this.proxy, false);
        try {
            final Bootstrap bootstrap = new Bootstrap();
            this.proxy.getScheduler().schedule(() -> {
                ProxiedClient.this.receivedCountAvg = receivedCount;
                ProxiedClient.this.sentCountAvg = sentCount;
                ProxiedClient.this.sentCount = 0;
                ProxiedClient.this.receivedCount = 0;
            }, 20);
            bootstrap.channelFactory(RakChannelFactory.client(NioDatagramChannel.class))
                .option(
                    RakChannelOption.RAK_PROTOCOL_VERSION,
                    NetworkConstants.CODEC.getRaknetProtocolVersion()
                )
                .option(RakChannelOption.RAK_METRICS, new RakChannelMetrics() {
                    @Override
                    public void bytesIn(int count) {
                        ProxiedClient.this.receivedCount += count;
                    }

                    @Override
                    public void bytesOut(int count) {
                        ProxiedClient.this.sentCount += count;
                    }
                })
                .group(new NioEventLoopGroup())
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
                .connect(this.serverAddress, this.address)
                .syncUninterruptibly()
                .channel();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        this.proxy.getLogger().info("Bedrock client connected to {}",
            this.serverAddress.toString());
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
        this.session.enableEncryption(key);
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