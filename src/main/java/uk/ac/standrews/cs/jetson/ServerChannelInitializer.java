package uk.ac.standrews.cs.jetson;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;

import java.lang.reflect.Method;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;

class ServerChannelInitializer extends BaseChannelInitializer {

    private final RequestDecoder request_decoder;
    private final ResponseEncoder response_encoder;
    private final ServerHandler server_handler;

    ServerChannelInitializer(final ChannelGroup channel_group, final Object service, final JsonFactory json_factory, final Map<String, Method> dispatch) {

        request_decoder = new RequestDecoder(json_factory, dispatch);
        response_encoder = new ResponseEncoder(json_factory);
        server_handler = new ServerHandler(channel_group, service);
    }

    @Override
    public void initChannel(final SocketChannel channel) throws Exception {

        super.initChannel(channel);
        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("encoder", request_decoder);
        pipeline.addLast("decoder", response_encoder);
        pipeline.addLast("handler", server_handler);
    }
}
