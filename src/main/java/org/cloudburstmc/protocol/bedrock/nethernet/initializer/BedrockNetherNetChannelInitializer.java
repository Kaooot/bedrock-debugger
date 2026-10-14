package org.cloudburstmc.protocol.bedrock.nethernet.initializer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.BedrockSession;
import org.cloudburstmc.protocol.bedrock.nethernet.BedrockNetherNetPeer;
import org.cloudburstmc.protocol.bedrock.nethernet.codec.NetherNetFrameCodec;
import org.cloudburstmc.protocol.bedrock.netty.codec.FrameIdCodec;
import org.cloudburstmc.protocol.bedrock.netty.codec.batch.BedrockBatchDecoder;
import org.cloudburstmc.protocol.bedrock.netty.codec.batch.BedrockBatchEncoder;
import org.cloudburstmc.protocol.bedrock.netty.codec.compression.CompressionCodec;
import org.cloudburstmc.protocol.bedrock.netty.codec.compression.CompressionStrategy;
import org.cloudburstmc.protocol.bedrock.netty.codec.compression.NoopCompression;
import org.cloudburstmc.protocol.bedrock.netty.codec.compression.SimpleCompressionStrategy;
import org.cloudburstmc.protocol.bedrock.netty.codec.packet.BedrockPacketCodec;
import org.cloudburstmc.protocol.bedrock.netty.codec.packet.BedrockPacketCodec_v3;

public abstract class BedrockNetherNetChannelInitializer<T extends BedrockSession> extends ChannelInitializer<Channel> {

    private static final BedrockBatchDecoder BATCH_DECODER = new BedrockBatchDecoder();
    private static final CompressionStrategy NOOP_STRATEGY = new SimpleCompressionStrategy(new NoopCompression());

    @Override
    protected final void initChannel(Channel channel) throws Exception {
        this.preInitChannel(channel);

        channel.pipeline()
                .addLast(FrameIdCodec.NAME, NetherNetFrameCodec.INSTANCE)
                .addLast(CompressionCodec.NAME, new CompressionCodec(NOOP_STRATEGY, false))
                .addLast(BedrockBatchDecoder.NAME, BATCH_DECODER)
                .addLast(BedrockBatchEncoder.NAME, new BedrockBatchEncoder());

        this.initPacketCodec(channel);

        channel.pipeline().addLast(BedrockPeer.NAME, this.createPeer(channel));

        this.postInitChannel(channel);
    }

    protected void preInitChannel(Channel channel) throws Exception {
    }

    protected void postInitChannel(Channel channel) throws Exception {
    }

    protected void initPacketCodec(Channel channel) throws Exception {
        channel.pipeline().addLast(BedrockPacketCodec.NAME, new BedrockPacketCodec_v3());
    }

    protected BedrockPeer createPeer(Channel channel) {
        return new BedrockNetherNetPeer(channel, this::createSession);
    }

    protected final T createSession(BedrockPeer peer, int subClientId) {
        T session = this.createSession0(peer, subClientId);
        this.initSession(session);
        return session;
    }

    protected abstract T createSession0(BedrockPeer peer, int subClientId);

    protected abstract void initSession(T session);
}
