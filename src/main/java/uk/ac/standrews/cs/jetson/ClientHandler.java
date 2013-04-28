package uk.ac.standrews.cs.jetson;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.concurrent.CyclicBarrier;

import uk.ac.standrews.cs.jetson.exception.JsonRpcError;
import uk.ac.standrews.cs.jetson.exception.JsonRpcException;
import uk.ac.standrews.cs.jetson.exception.UnexpectedException;

@Sharable
class ClientHandler extends ChannelInboundMessageHandlerAdapter<Response> {

    static final AttributeKey<Response> RESPONSE_ATTRIBUTE = new AttributeKey<Response>("response");
    static final AttributeKey<Request> REQUEST_ATTRIBUTE = new AttributeKey<Request>("request");
    static final AttributeKey<CyclicBarrier> RESPONSE_BARRIER_ATTRIBUTE = new AttributeKey<CyclicBarrier>("response_latch");

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final Response response) throws Exception {

        ctx.channel().attr(RESPONSE_ATTRIBUTE).set(response);
        ctx.channel().attr(RESPONSE_BARRIER_ATTRIBUTE).get().await();
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {

        ctx.close();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {

        cause.printStackTrace();
        final CyclicBarrier latch = ctx.channel().attr(RESPONSE_BARRIER_ATTRIBUTE).get();
        if (latch != null) {
            final Attribute<Request> id_attr = ctx.channel().attr(ClientHandler.REQUEST_ATTRIBUTE);
            final Long request_id = id_attr == null ? null : id_attr.get().getId();
            final Response response = ctx.channel().attr(RESPONSE_ATTRIBUTE).get();
            final JsonRpcError error;
            if (cause instanceof JsonRpcException) {
                error = JsonRpcException.class.cast(cause);
            }
            else {
                error = new UnexpectedException(cause);
            }
            response.setId(request_id);
            response.setError(error);
            latch.reset();
        }
        else {
            ctx.close();
        }
    }
}
