package org.cloudburstmc.protocol.bedrock.nethernet;

import io.netty.channel.Channel;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.BedrockSessionFactory;
import org.cloudburstmc.protocol.bedrock.data.connection.PacketCompressionAlgorithm;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockChannelInitializer;

public class BedrockNetherNetPeer extends BedrockPeer {

    private static final int COMPRESSION_ERA = 11;

    public BedrockNetherNetPeer(Channel channel, BedrockSessionFactory sessionFactory) {
        super(channel, sessionFactory);
    }

    @Override
    public void setCompression(PacketCompressionAlgorithm algorithm) {
        this.setCompression(BedrockChannelInitializer.getCompression(algorithm, COMPRESSION_ERA, false));
    }
}
