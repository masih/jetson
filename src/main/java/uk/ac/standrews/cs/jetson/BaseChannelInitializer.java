package uk.ac.standrews.cs.jetson;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.concurrent.TimeUnit;

abstract class BaseChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final long DEFAULT_READ_TIMEOUT_IN_SECONDS = 300;
    private static final long DEFAULT_WRITE_TIMEOUT_IN_SECONDS = 300;
    private final long read_timeout;
    private final long write_timeout;
    private final TimeUnit timeout_unit;

    public BaseChannelInitializer() {

        this(DEFAULT_READ_TIMEOUT_IN_SECONDS, DEFAULT_WRITE_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    }

    public BaseChannelInitializer(final long read_timeout, final long write_timeout, final TimeUnit timeout_unit) {

        this.read_timeout = read_timeout;
        this.write_timeout = write_timeout;
        this.timeout_unit = timeout_unit;
    }

    private WriteTimeoutHandler createWriteTimeoutHandler() {

        return new WriteTimeoutHandler(write_timeout, timeout_unit);
    }

    private ReadTimeoutHandler createReadTimeoutHandler() {

        return new ReadTimeoutHandler(read_timeout, timeout_unit);
    }

    @Override
    public void initChannel(final SocketChannel channel) throws Exception {

        channel.pipeline().addLast("read_timeout", createWriteTimeoutHandler());
        channel.pipeline().addLast("write_timeout", createReadTimeoutHandler());
        channel.pipeline().addLast(FrameDecoder.NAME, createFrameDecoder());
    }

    protected FrameDecoder createFrameDecoder() {

        return new FrameDecoder();
    }
}
