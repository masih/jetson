package uk.ac.standrews.cs.jetson;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.util.AttributeKey;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CountDownLatch;

import uk.ac.standrews.cs.jetson.exception.JsonRpcError;
import uk.ac.standrews.cs.jetson.exception.JsonRpcException;
import uk.ac.standrews.cs.jetson.exception.TransportException;
import uk.ac.standrews.cs.jetson.exception.UnexpectedException;

@Sharable
public class JsonRpcClientHandler extends ChannelInboundMessageHandlerAdapter<JsonRpcResponse> {

    public static final AttributeKey<JsonRpcResponse> RESPONSE_ATTRIBUTE = new AttributeKey<JsonRpcResponse>("response");

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final JsonRpcResponse response) throws Exception {

        ctx.channel().attr(RESPONSE_ATTRIBUTE).set(response);
        final CountDownLatch latch = ctx.channel().attr(JsonRpcRequestEncoder.RESPONSE_LATCH).get();
        if (latch == null) {
            System.out.println(response);
        }
        else {
            latch.countDown();
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {

        throw new TransportException(new ClosedChannelException());
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {

        try {
            final long request_id = ctx.channel().attr(JsonRpcResponseDecoder.REQUEST_ID_ATTRIBUTE).get();
            final JsonRpcError error;
            if (cause instanceof JsonRpcException) {
                error = JsonRpcException.class.cast(cause);
            }
            else {
                error = new UnexpectedException(cause);
            }
            ctx.channel().attr(RESPONSE_ATTRIBUTE).set(new JsonRpcResponse.JsonRpcResponseError(request_id, error));
            ctx.channel().attr(JsonRpcRequestEncoder.RESPONSE_LATCH).get().countDown();
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
