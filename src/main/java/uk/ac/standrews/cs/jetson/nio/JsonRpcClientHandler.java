package uk.ac.standrews.cs.jetson.nio;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.util.AttributeKey;
import uk.ac.standrews.cs.jetson.JsonRpcResponse;

@Sharable
public class JsonRpcClientHandler extends ChannelInboundMessageHandlerAdapter<JsonRpcResponse> {

    static final AttributeKey<JsonRpcResponse> RESPONSE_ATTRIBUTE = new AttributeKey<JsonRpcResponse>("response");

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final JsonRpcResponse response) throws Exception {

        ctx.channel().close().addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(final ChannelFuture future) {

                ctx.channel().attr(RESPONSE_ATTRIBUTE).set(response);
                ctx.channel().attr(JsonRpcRequestEncoder.RESPONSE_LATCH).get().countDown();
            }
        });
        //        final boolean offered = responses.offer(response);
        //        assert offered;
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {

        cause.printStackTrace();
    }
}
