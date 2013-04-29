/*
 * Copyright 2013 Masih Hajiarabderkani
 * 
 * This file is part of Jetson.
 * 
 * Jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jetson.  If not, see <http://www.gnu.org/licenses/>.
 */
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