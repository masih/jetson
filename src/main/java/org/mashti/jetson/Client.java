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
    protected final InetSocketAddress address;
    private final Method[] dispatch;
    private final ChannelFuturePool channel_pool;
    private volatile WrittenByteCountListener written_byte_count_listener;

    protected Client(final InetSocketAddress address, final Method[] dispatch, final ChannelFuturePool channel_pool) {

        this.address = address;
        this.dispatch = dispatch;
        this.channel_pool = channel_pool;

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

    protected FutureResponse writeRequest(final FutureResponse future_response) {

        final ChannelFuture channel_future = channel_pool.get(address);
        final GenericFutureListener<ChannelFuture> listener = new WriteRequestListener(channel_future, future_response);
        channel_future.addListener(listener);
        return future_response;
    }

    protected static void setException(final Throwable cause, final FutureResponse future_response) {

        RPCException rpc_error = cause instanceof RPCException ? (RPCException) cause : new TransportException(cause);
        future_response.setException(rpc_error);
    }

    protected void beforeFlush(final Channel channel, final FutureResponse future_response) throws RPCException {

        // Do nothing; reserved for customization via extending classes
    }

    protected boolean dispatchContains(final Method target) {

        for (final Method method : dispatch) {
            if (method.equals(target)) { return true; }
        }
        return false;
    }

    private FutureResponse writeRequest(final Method method, final Object[] params) {

        final FutureResponse future_response = newFutureResponse(method, params);
        return writeRequest(future_response);
    }

    protected static class ExceptionListener implements GenericFutureListener<ChannelFuture> {

        private final FutureResponse future_response;

        public ExceptionListener(final FutureResponse future_response) {

            this.future_response = future_response;
        }

        @Override
        public void operationComplete(final ChannelFuture future) throws Exception {

            if (!future.isSuccess()) {
                setException(future.cause(), future_response);
            }
        }
    }

    protected class WriteRequestListener implements GenericFutureListener<ChannelFuture> {

        private final ChannelFuture channel_future;
        private final FutureResponse future_response;

        public WriteRequestListener(final ChannelFuture channel_future, final FutureResponse future_response) {

            this.channel_future = channel_future;
            this.future_response = future_response;
        }

        @Override
        public void operationComplete(final ChannelFuture future) throws Exception {

            if (future.isSuccess()) {
                final Channel channel = channel_future.channel();
                final ChannelFuture write = channel.write(future_response);
                write.addListener(new ExceptionListener(future_response));
                beforeFlush(channel, future_response);
                channel.flush();
            }
            else {
                setException(future.cause(), future_response);
            }
        }
    }
}
