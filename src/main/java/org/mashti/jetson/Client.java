/**
 * Copyright © 2015, Masih H. Derkani
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mashti.jetson;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
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
    protected volatile WrittenByteCountListener written_byte_count_listener;

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

            final FutureResponse<?> future_response = writeRequest(method, params);
            try {
                return future_response;
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

        return "Client{" + "address=" + address + '}';
    }

    public FutureResponse<?> newFutureResponse(final Method method, final Object[] arguments) {

        final FutureResponse<?> response = new FutureResponse();
        response.setMethod(method);
        response.setArguments(arguments);
        response.setWrittenByteCountListener(written_byte_count_listener);
        return response;
    }

    protected FutureResponse<?> writeRequest(final FutureResponse<?> future_response) {

        final ChannelFuture channel_future = channel_pool.get(address);
        final GenericFutureListener<ChannelFuture> listener = new WriteRequestListener(channel_future, future_response);
        channel_future.addListener(listener);
        return future_response;
    }

    protected static void setException(final Throwable cause, final FutureResponse<?> future_response) {

        RPCException rpc_error = cause instanceof RPCException ? (RPCException) cause : new TransportException(cause);
        future_response.completeExceptionally(rpc_error);
    }

    protected void beforeFlush(final Channel channel, final FutureResponse<?> future_response) throws RPCException {

        // Do nothing; reserved for customization via extending classes
    }

    protected boolean dispatchContains(final Method target) {

        for (final Method method : dispatch) {
            if (method.equals(target)) { return true; }
        }
        return false;
    }

    private FutureResponse<?> writeRequest(final Method method, final Object[] params) {

        final FutureResponse<?> future_response = newFutureResponse(method, params);
        return writeRequest(future_response);
    }

    protected void writeToChannel(Channel channel, FutureResponse future_response) {

        final ChannelFuture write = channel.write(future_response);
        write.addListener(new ExceptionListener(future_response));
    }

    protected static class ExceptionListener implements GenericFutureListener<ChannelFuture> {

        private final FutureResponse<?> future_response;

        public ExceptionListener(final FutureResponse<?> future_response) {

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
        private final FutureResponse<?> future_response;

        public WriteRequestListener(final ChannelFuture channel_future, final FutureResponse<?> future_response) {

            this.channel_future = channel_future;
            this.future_response = future_response;
        }

        @Override
        public void operationComplete(final ChannelFuture future) throws Exception {

            if (future.isSuccess()) {
                final Channel channel = channel_future.channel();
                writeToChannel(channel, future_response);
                beforeFlush(channel, future_response);
                channel.flush();
            }
            else {
                setException(future.cause(), future_response);
            }
            future.removeListener(this);
        }
    }
}
