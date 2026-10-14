package org.cloudburstmc.protocol.bedrock.nethernet.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.cloudburstmc.protocol.bedrock.netty.BedrockBatchWrapper;

import java.util.List;

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
