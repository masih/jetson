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
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
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
    private final ChannelPool channel_pool;
    private final Method[] dispatch;
    private volatile WrittenByteCountListener written_byte_count_listener;

    protected Client(final InetSocketAddress address, final Method[] dispatch, final ChannelPool channel_pool) {

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
            catch (CancellationException e) {
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

    protected List<FutureResponse> getExtraRequests() throws RPCException {

        return null;
    }

    FutureResponse writeRequest(final Method method, final Object[] params) throws RPCException {

        final FutureResponse future_response = newFutureResponse(method, params);
        return writeRequest(future_response);
    }

    protected FutureResponse writeRequest(FutureResponse future_response) throws RPCException {

        List<FutureResponse> requests = getExtraRequests();
        if (requests == null) {
            requests = new ArrayList<FutureResponse>();
        }

        requests.add(0, future_response);

        final Channel channel = borrowChannel();
        try {
            for (FutureResponse response : requests) {
                channel.write(response);
            }
            channel.flush();
        }
        finally {
            returnChannel(channel);
        }
        return future_response;
    }

    boolean dispatchContains(final Method target) {

        for (final Method method : dispatch) {
            if (method.equals(target)) { return true; }
        }
        return false;
    }

    private synchronized Channel borrowChannel() throws InternalServerException, TransportException {

        try {
            return channel_pool.borrowObject();
        }
        catch (final InterruptedException e) {
            throw new InternalServerException(e);
        }
        catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) { throw new TransportException(cause); }
            throw new InternalServerException(e);
        }
        catch (final NoSuchElementException e) {
            throw new TransportException(e);
        }
        catch (final Exception e) {
            throw new InternalServerException(e);
        }
    }

    private void returnChannel(final Channel channel) throws InternalServerException {

        try {
            channel_pool.returnObject(channel);
        }
        catch (final Exception e) {
            throw new InternalServerException(e);
        }
    }
}
