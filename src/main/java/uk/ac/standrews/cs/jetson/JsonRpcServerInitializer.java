package uk.ac.standrews.cs.jetson;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;

import java.lang.reflect.Method;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;

public class JsonRpcServerInitializer extends ChannelInitializer<SocketChannel> {

    private final JsonRpcRequestDecoder request_decoder;
    private final JsonRpcResponseEncoder response_encoder;
    private final JsonRpcServerHandler server_handler;

    public JsonRpcServerInitializer(final ChannelGroup channel_group, final Object service, final JsonFactory json_factory, final Map<String, Method> dispatch) {

        request_decoder = new JsonRpcRequestDecoder(json_factory, dispatch);
        response_encoder = new JsonRpcResponseEncoder(json_factory);
        server_handler = new JsonRpcServerHandler(channel_group, service);
    }

    @Override
    public void initChannel(final SocketChannel channel) throws Exception {

        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addFirst("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast("encoder", request_decoder);
        pipeline.addLast("decoder", response_encoder);
        pipeline.addLast("handler", server_handler);
    }
}
