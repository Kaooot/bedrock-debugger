package org.cloudburstmc.protocol.bedrock.nethernet.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.cloudburstmc.protocol.bedrock.netty.BedrockBatchWrapper;

import java.util.List;

/**
 * NetherNet's equivalent of {@code FrameIdCodec}, bridging the transport's raw {@link ByteBuf}
 * messages and the {@link BedrockBatchWrapper} the rest of the stock Bedrock pipeline
 * (compression, batch, packet codecs) works with.
 * <p>
 * Unlike RakNet there is no frame-id byte to add or strip: the WebRTC data channel delivers exactly
 * one compressed batch per message, so framing is a pass-through. Registered under
 * {@code FrameIdCodec.NAME} so the stock {@code BedrockPeer} can add the encryption handlers right
 * after it, exactly as it does for RakNet.
 */
@Sharable
public class NetherNetFrameCodec extends MessageToMessageCodec<ByteBuf, BedrockBatchWrapper> {

    public static final NetherNetFrameCodec INSTANCE = new NetherNetFrameCodec();

    @Override
    protected void encode(ChannelHandlerContext ctx, BedrockBatchWrapper msg, List<Object> out) {
        if (msg.getCompressed() == null) {
            throw new IllegalStateException("Bedrock batch was not compressed");
        }
        out.add(msg.getCompressed().retainedSlice());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        if (!msg.isReadable()) {
            return;
        }
        out.add(BedrockBatchWrapper.newInstance(msg.readRetainedSlice(msg.readableBytes()), null));
    }
}
