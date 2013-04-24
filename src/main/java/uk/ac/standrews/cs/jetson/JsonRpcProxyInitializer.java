package uk.ac.standrews.cs.jetson;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;

import com.fasterxml.jackson.core.JsonFactory;

public class JsonRpcProxyInitializer extends ChannelInitializer<SocketChannel> {

    private final JsonRpcRequestEncoder request_encoder;
    private final JsonRpcResponseDecoder response_decoder;
    private final JsonRpcClientHandler client_handler;

    public JsonRpcProxyInitializer(final JsonFactory json_factory) {

        response_decoder = new JsonRpcResponseDecoder(json_factory);
        request_encoder = new JsonRpcRequestEncoder(json_factory);
        client_handler = new JsonRpcClientHandler();
    }

    @Override
    public void initChannel(final SocketChannel channel) throws Exception {

        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast("encoder", request_encoder);
        pipeline.addLast("decoder", response_decoder);
        pipeline.addLast("handler", client_handler);
    }
}
