package uk.ac.standrews.cs.jetson;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import com.fasterxml.jackson.core.JsonFactory;

class ClientChannelInitializer extends BaseChannelInitializer {

    private final RequestEncoder request_encoder;
    private final ResponseDecoder response_decoder;
    private final ClientHandler client_handler;

    ClientChannelInitializer(final JsonFactory json_factory) {

        response_decoder = new ResponseDecoder(json_factory);
        request_encoder = new RequestEncoder(json_factory);
        client_handler = new ClientHandler();
    }

    @Override
    public void initChannel(final SocketChannel channel) throws Exception {

        super.initChannel(channel);
        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("encoder", request_encoder);
        pipeline.addLast("decoder", response_decoder);
        pipeline.addLast("handler", client_handler);
    }
}
