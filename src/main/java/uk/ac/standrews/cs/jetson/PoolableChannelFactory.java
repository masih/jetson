package uk.ac.standrews.cs.jetson;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.InetSocketAddress;

import org.apache.commons.pool.BasePoolableObjectFactory;

class PoolableChannelFactory extends BasePoolableObjectFactory<Channel> {

    private final Bootstrap bootstrap;
    private final InetSocketAddress address;

    PoolableChannelFactory(final Bootstrap bootstrap, final InetSocketAddress address) {

        this.bootstrap = bootstrap;
        this.address = address;
    }

    @Override
    public Channel makeObject() throws Exception {

        final ChannelFuture connect_future = bootstrap.connect(address);
        connect_future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        connect_future.await().get();
        return connect_future.sync().channel();
    }

    @Override
    public void destroyObject(final Channel channel) {

        channel.close();
    }

    @Override
    public void passivateObject(final Channel channel) throws Exception {

        channel.attr(JsonRpcClientHandler.RESPONSE_ATTRIBUTE).remove();
        channel.attr(JsonRpcRequestEncoder.RESPONSE_LATCH_ATTRIBUTE).remove();
    }

    @Override
    public boolean validateObject(final Channel channel) {

        return channel.isOpen() && channel.isActive() && super.validateObject(channel);
    }

}
