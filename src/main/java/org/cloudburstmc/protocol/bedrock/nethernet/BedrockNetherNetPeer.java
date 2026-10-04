package org.cloudburstmc.protocol.bedrock.nethernet;

import io.netty.channel.Channel;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.BedrockSessionFactory;
import org.cloudburstmc.protocol.bedrock.data.connection.PacketCompressionAlgorithm;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockChannelInitializer;

/**
 * A {@link BedrockPeer} for NetherNet connections. It reuses the stock pipeline (compression,
 * batch and packet codecs plus encryption), so the only thing it has to change is how a compression
 * algorithm maps to a strategy: the stock peer derives that from the RakNet protocol version, which
 * a NetherNet channel does not have.
 */
public class BedrockNetherNetPeer extends BedrockPeer {

    // NetherNet matches the current RakNet compression era (raw zlib, prefixed by algorithm byte).
    private static final int COMPRESSION_ERA = 11;

    public BedrockNetherNetPeer(Channel channel, BedrockSessionFactory sessionFactory) {
        super(channel, sessionFactory);
    }

    @Override
    public void setCompression(PacketCompressionAlgorithm algorithm) {
        // Skip the stock getRakVersion() lookup (RAK_PROTOCOL_VERSION is unset on a NetherNet
        // channel) and select the strategy directly; setCompression(CompressionStrategy) then adds
        // the prefix based on the codec's protocol version, like RakNet.
        this.setCompression(BedrockChannelInitializer.getCompression(algorithm, COMPRESSION_ERA, false));
    }
}
