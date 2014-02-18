/**
 * This file is part of jetson.
 *
 * jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jetson.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mashti.jetson;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import org.mashti.jetson.exception.InternalServerException;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Client implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private final InetSocketAddress address;
    private final Method[] dispatch;
    private final Bootstrap bootstrap;
    private volatile WrittenByteCountListener written_byte_count_listener;
    private volatile Channel channel;

    protected Client(final InetSocketAddress address, final Method[] dispatch, final Bootstrap bootstrap) {

        this.address = address;
        this.dispatch = dispatch;
        this.bootstrap = bootstrap;
    }

    public InetSocketAddress getAddress() {

        return address;
    }

    public void setWrittenByteCountListener(WrittenByteCountListener listener) {

        written_byte_count_listener = listener;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] params) throws Throwable {

        if (dispatchContains(method)) {
            final FutureResponse future_response = writeRequest(method, params);

            try {
                return future_response.get();
            }
            catch (InterruptedException e) {
                throw new InternalServerException(e);
            }
            catch (ExecutionException e) {
                throw e.getCause();
            }
            catch (Exception e) {
                throw new InternalServerException(e);
            }
        }
        else {
            LOGGER.debug("method {} was not found in dispatch; executing method on proxy object", method);
            return method.invoke(this, params);
        }
    }

    @Override
    public String toString() {

        return new StringBuilder("Client{").append("address=").append(address).append('}').toString();
    }

    public FutureResponse newFutureResponse(final Method method, final Object[] arguments) {

        final FutureResponse response = new FutureResponse();
        response.setMethod(method);
        response.setArguments(arguments);
        response.setWrittenByteCountListener(written_byte_count_listener);
        return response;
    }

    protected FutureResponse writeRequest(final FutureResponse future_response) throws RPCException {

        final Channel channel = getChannel();
        final ChannelFuture write = channel.write(future_response);
        write.addListener(new GenericFutureListener<ChannelFuture>() {

            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {

                if (!future.isSuccess()) {
                    final Throwable cause = future.cause();
                    RPCException rpc_error = cause instanceof RPCException ? (RPCException) cause : new RPCException(cause);
                    future_response.setException(rpc_error);
                }
            }
        });
        beforeFlush(channel, future_response);
        channel.flush();
        return future_response;
    }

    protected void beforeFlush(final Channel channel, final FutureResponse future_response) throws RPCException {

        // Do nothing; reserved for customization via extending classes
    }

    boolean dispatchContains(final Method target) {

        for (final Method method : dispatch) {
            if (method.equals(target)) { return true; }
        }
        return false;
    }

    private FutureResponse writeRequest(final Method method, final Object[] params) throws RPCException {

        final FutureResponse future_response = newFutureResponse(method, params);
        return writeRequest(future_response);
    }

    private synchronized Channel getChannel() throws RPCException {

        if (channel == null || !channel.isActive()) {
            try {
                channel = ChannelUtils.create(address, bootstrap);
            }
            catch (InterruptedException e) {
                throw new InternalServerException(e);
            }
            catch (Exception e) {
                throw e instanceof RPCException ? (RPCException) e : new TransportException(e);
            }
        }
        return channel;
    }
}
