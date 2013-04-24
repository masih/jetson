package uk.ac.standrews.cs.jetson;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseError;
import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseResult;
import uk.ac.standrews.cs.jetson.exception.AccessException;
import uk.ac.standrews.cs.jetson.exception.InternalException;
import uk.ac.standrews.cs.jetson.exception.InvalidParameterException;
import uk.ac.standrews.cs.jetson.exception.InvocationException;
import uk.ac.standrews.cs.jetson.exception.JsonRpcException;
import uk.ac.standrews.cs.jetson.exception.ServerException;
import uk.ac.standrews.cs.jetson.exception.ServerRuntimeException;

@Sharable
public class JsonRpcServerHandler extends ChannelInboundMessageHandlerAdapter<JsonRpcRequest> {

    private static final Logger LOGGER = Logger.getLogger(JsonRpcServerHandler.class.getName());
    private final Object service;

    public JsonRpcServerHandler(final Object service) {

        this.service = service;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final JsonRpcRequest request) throws Exception {

        final JsonRpcResponse response = handleRequest(request);
        ctx.write(response);
    }

    private JsonRpcResponseResult handleRequest(final JsonRpcRequest request) throws ServerException {

        final Method method = request.getMethod();
        final Object[] parameters = request.getParameters();

        try {
            final Object result = invoke(method, parameters);
            return new JsonRpcResponseResult(request.getId(), result);
        }
        catch (final IllegalArgumentException e) {
            throw new InvalidParameterException(e);
        }
        catch (final RuntimeException e) {
            throw new ServerRuntimeException(e);
        }
        catch (final InvocationTargetException e) {
            throw new InvocationException(e);
        }
        catch (final IllegalAccessException e) {
            throw new AccessException(e);
        }
        catch (final ExceptionInInitializerError e) {
            throw new InternalException(e);
        }
    }

    private Object invoke(final Method method, final Object... parameters) throws IllegalAccessException, InvocationTargetException {

        return method.invoke(service, parameters);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {

        if (ctx.channel().isOpen()) {
            final long current_request_id = ctx.channel().attr(JsonRpcResponseDecoder.REQUEST_ID_ATTRIBUTE).get();
            final JsonRpcException exception = JsonRpcException.class.isInstance(cause) ? JsonRpcException.class.cast(cause) : new InternalException(cause);

            final JsonRpcResponseError error = new JsonRpcResponseError(current_request_id, exception);
            try {
                ctx.write(error);
            }
            catch (final Throwable e) {
                LOGGER.log(Level.FINE, "failed to notify JSON RPC error", e);
                ctx.close();
            }
        }
    }
}
