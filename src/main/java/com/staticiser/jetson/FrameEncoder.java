package com.staticiser.jetson;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
class FrameEncoder extends MessageToByteEncoder<ByteBuf> {

    static final String NAME = "frame_encoder";

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf msg, final ByteBuf out) throws Exception {
        out.writeBytes(msg);
        out.writeBytes(FrameDecoder.FRAME_DELIMITER_BYTES);
    }
}
