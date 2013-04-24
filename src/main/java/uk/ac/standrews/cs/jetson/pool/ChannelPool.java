package uk.ac.standrews.cs.jetson.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

import org.apache.commons.pool.impl.GenericObjectPool;

public class ChannelPool extends GenericObjectPool<Channel> {

    public ChannelPool(final Bootstrap bootstrap, final InetSocketAddress address) {

        super(new PoolableChannelFactory(bootstrap, address));
    }


}
