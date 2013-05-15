/*
 * Copyright 2013 Masih Hajiarabderkani
 * 
 * This file is part of Jetson.
 * 
 * Jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jetson.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.staticiser.jetson;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.util.AttributeKey;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.staticiser.jetson.exception.AccessException;
import com.staticiser.jetson.exception.InternalException;
import com.staticiser.jetson.exception.InvalidParameterException;
import com.staticiser.jetson.exception.InvocationException;
import com.staticiser.jetson.exception.JsonRpcError;
import com.staticiser.jetson.exception.JsonRpcException;
import com.staticiser.jetson.exception.ServerException;
import com.staticiser.jetson.exception.ServerRuntimeException;
import com.staticiser.jetson.exception.UnexpectedException;

@Sharable
class RequestHandler extends ChannelInboundMessageHandlerAdapter<Request> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);
    private static final AttributeKey<Future<Void>> PROCESSING_FUTURE_ATTRIBUTE = new AttributeKey<Future<Void>>("processing");
    private final ExecutorService executor;

    static final String NAME = "request_handler";

    RequestHandler(final ExecutorService executor) {

        this.executor = executor;
    }

    @Override
    public void channelActive(final ChannelHandlerContext context) throws Exception {

        final Channel channel = context.channel();
        channel.attr(ResponseHandler.RESPONSE_ATTRIBUTE).set(new Response());
        super.channelActive(context);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext context) throws Exception {

        cancelRequestProcessing(context);
        context.channel().attr(PROCESSING_FUTURE_ATTRIBUTE).remove();
        context.channel().attr(ResponseHandler.RESPONSE_ATTRIBUTE).remove();
        context.close();
        super.channelInactive(context);
    }

    private void cancelRequestProcessing(final ChannelHandlerContext context) {

        final Future<Void> procc = context.channel().attr(PROCESSING_FUTURE_ATTRIBUTE).get();
        if (procc != null) {
            procc.cancel(true);
        }
    }

    @Override
    public void messageReceived(final ChannelHandlerContext context, final Request request) throws Exception {

        final Future<Void> processing_future = executor.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                try {
                    final Object service = context.channel().parent().attr(Server.SERVICE_ATTRIBUTE).get();
                    final Object result = handleRequest(service, request);
                    final Response response = context.channel().attr(ResponseHandler.RESPONSE_ATTRIBUTE).get();
                    response.reset();
                    response.setId(request.getId());
                    response.setResult(result);
                    context.write(response);
                }
                catch (final Throwable e) {
                    exceptionCaught(context, e);
                }
                return null;
            }
        });
        context.channel().attr(PROCESSING_FUTURE_ATTRIBUTE).set(processing_future);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) throws Exception {

        LOGGER.debug("caught on server handler", cause);
        cancelRequestProcessing(context);
        if (isRecoverable(context, cause)) {
            final Request request = context.channel().attr(ResponseHandler.REQUEST_ATTRIBUTE).get();
            final Long current_request_id = request.getId();
            final Response response = context.channel().attr(ResponseHandler.RESPONSE_ATTRIBUTE).get();
            final JsonRpcError error;
            if (cause instanceof JsonRpcException) {
                error = JsonRpcException.class.cast(cause);
            }
            else if (cause instanceof RuntimeException) {
                error = new ServerRuntimeException(cause);
            }
            else {
                error = new UnexpectedException(cause);
            }
            response.setError(error);
            response.setId(current_request_id);
            context.write(response);
        }
        else {
            LOGGER.debug("closing context after unrecoverable error", cause);
            context.close();
        }
    }

    private boolean isRecoverable(final ChannelHandlerContext context, final Throwable cause) {

        return !(cause instanceof ChannelException) && context.channel().isOpen();
    }

    private Object handleRequest(final Object service, final Request request) throws ServerException {

        final Method method = request.getMethod();
        final Object[] parameters = request.getParameters();

        try {
            return method.invoke(service, parameters);
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
}
