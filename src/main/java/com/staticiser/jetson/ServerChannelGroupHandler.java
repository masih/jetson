package com.staticiser.jetson;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelStateHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;

@Sharable
final class ServerChannelGroupHandler extends ChannelStateHandlerAdapter {

    static final AttributeKey<ChannelGroup> CHANNEL_GROUP_ATTRIBUTE = new AttributeKey<ChannelGroup>("ChannelGroup");

    @Override
    public void inboundBufferUpdated(final ChannelHandlerContext ctx) throws Exception {

        ctx.fireInboundBufferUpdated();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {

        final Channel channel = ctx.channel();
        final ChannelGroup group = channel.parent().attr(CHANNEL_GROUP_ATTRIBUTE).get();
        group.add(channel);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {

        final Channel channel = ctx.channel();
        final ChannelGroup group = channel.parent().attr(CHANNEL_GROUP_ATTRIBUTE).get();
        group.remove(channel);
        super.channelInactive(ctx);
    }
}
