package uk.ac.standrews.cs.jetson.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool.BasePoolableObjectFactory;

public class PoolableChannelFactory extends BasePoolableObjectFactory<Channel> {

    private final Bootstrap bootstrap;
    private final InetSocketAddress address;
    private static final AtomicInteger CHANNELS = new AtomicInteger();

    public PoolableChannelFactory(final Bootstrap bootstrap, final InetSocketAddress address) {

        this.bootstrap = bootstrap;
        this.address = address;

    }



    @Override
    public Channel makeObject() throws Exception {
        final ChannelFuture connect_future = bootstrap.connect(address);
        connect_future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        connect_future.await().get();
        System.out.println(CHANNELS.incrementAndGet());
        return connect_future.sync().channel();
    }

    @Override
    public void destroyObject(final Channel channel) {

        channel.close();
    }

    @Override
    public boolean validateObject(final Channel channel) {

        return channel.isOpen() && super.validateObject(channel);
    }

}
