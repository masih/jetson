package uk.ac.standrews.cs.jetson;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.InetSocketAddress;
import java.util.concurrent.CyclicBarrier;

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
        final Channel channel = connect_future.sync().channel();
        configureChannel(channel);
        return channel;
    }

    private void configureChannel(final Channel channel) {

        channel.attr(ClientHandler.RESPONSE_BARRIER_ATTRIBUTE).set(new CyclicBarrier(2));
        channel.attr(ClientHandler.REQUEST_ATTRIBUTE).set(new Request());
        channel.attr(ClientHandler.RESPONSE_ATTRIBUTE).set(new Response());
    }

    @Override
    public void destroyObject(final Channel channel) {

        channel.close();
    }

    @Override
    public void passivateObject(final Channel channel) throws Exception {

        channel.attr(ClientHandler.REQUEST_ATTRIBUTE).get().reset();
        channel.attr(ClientHandler.RESPONSE_BARRIER_ATTRIBUTE).get().reset();
        channel.attr(ClientHandler.RESPONSE_ATTRIBUTE).get().reset();
    }

    @Override
    public boolean validateObject(final Channel channel) {

        return channel.isOpen() && channel.isActive() && super.validateObject(channel);
    }

}
