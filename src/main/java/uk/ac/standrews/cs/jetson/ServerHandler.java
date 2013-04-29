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
package uk.ac.standrews.cs.jetson;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.group.ChannelGroup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.standrews.cs.jetson.exception.AccessException;
import uk.ac.standrews.cs.jetson.exception.InternalException;
import uk.ac.standrews.cs.jetson.exception.InvalidParameterException;
import uk.ac.standrews.cs.jetson.exception.InvocationException;
import uk.ac.standrews.cs.jetson.exception.JsonRpcException;
import uk.ac.standrews.cs.jetson.exception.ServerException;
import uk.ac.standrews.cs.jetson.exception.ServerRuntimeException;

@Sharable
class ServerHandler extends ChannelInboundMessageHandlerAdapter<Request> {

    private static final Logger LOGGER = Logger.getLogger(ServerHandler.class.getName());
    private final Object service;
    private final ChannelGroup channel_group;

    ServerHandler(final ChannelGroup channel_group, final Object service) {

        this.channel_group = channel_group;
        this.service = service;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {

        final Channel channel = ctx.channel();
        channel.attr(ClientHandler.RESPONSE_ATTRIBUTE).set(new Response());
        channel_group.add(channel);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {

        ctx.channel().attr(ClientHandler.RESPONSE_ATTRIBUTE).remove();
        ctx.close();
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final Request request) throws Exception {

        final Object result = handleRequest(request);
        final Response response = ctx.channel().attr(ClientHandler.RESPONSE_ATTRIBUTE).get();
        response.reset();
        response.setId(request.getId());
        response.setResult(result);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {

        //        cause.printStackTrace();
        if (ctx.channel().isOpen()) {
            final Long current_request_id = ctx.channel().attr(ClientHandler.REQUEST_ATTRIBUTE).get().getId();
            final Response response = ctx.channel().attr(ClientHandler.RESPONSE_ATTRIBUTE).get();
            final JsonRpcException exception = JsonRpcException.class.isInstance(cause) ? JsonRpcException.class.cast(cause) : new InternalException(cause);
            response.setError(exception);
            response.setId(current_request_id);
            try {
                ctx.write(response);
            }
            catch (final Throwable e) {
                LOGGER.log(Level.FINE, "failed to notify JSON RPC error", e);
                ctx.close();
            }
        }
        else {
            ctx.close();
        }
    }

    private Object handleRequest(final Request request) throws ServerException {

        final Method method = request.getMethod();
        final Object[] parameters = request.getParameters();

        try {
            return invoke(method, parameters);
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

}
