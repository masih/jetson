package uk.ac.standrews.cs.jetson;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.util.AttributeKey;

@Sharable
public class JsonRpcClientHandler extends ChannelInboundMessageHandlerAdapter<JsonRpcResponse> {

    static final AttributeKey<JsonRpcResponse> RESPONSE_ATTRIBUTE = new AttributeKey<JsonRpcResponse>("response");

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final JsonRpcResponse response) throws Exception {

        ctx.channel().attr(RESPONSE_ATTRIBUTE).set(response);
        ctx.channel().attr(JsonRpcRequestEncoder.RESPONSE_LATCH).get().countDown();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {

        cause.printStackTrace();
    }
}
