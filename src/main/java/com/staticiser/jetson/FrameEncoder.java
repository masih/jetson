package com.staticiser.jetson;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToByteEncoder;

@ChannelHandler.Sharable
class FrameEncoder extends ByteToByteEncoder {

    static final String NAME = "frame_encoder";

    @Override
    protected void encode(final ChannelHandlerContext context, final ByteBuf in, final ByteBuf out) throws Exception {

        out.writeBytes(in);
        out.writeBytes(FrameDecoder.FRAME_DELIMITER_BYTES);
    }
}
