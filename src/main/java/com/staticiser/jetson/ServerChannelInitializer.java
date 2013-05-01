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
package com.staticiser.jetson;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.fasterxml.jackson.core.JsonFactory;

class ServerChannelInitializer extends BaseChannelInitializer {

    private final RequestDecoder request_decoder;
    private final ResponseEncoder response_encoder;
    private final RequestHandler request_handler;
    private final ServerChannelGroupHandler channel_group_handler;

    ServerChannelInitializer(final JsonFactory json_factory, final Map<String, Method> dispatch, final ExecutorService executor) {

        request_decoder = new RequestDecoder(json_factory, dispatch);
        response_encoder = new ResponseEncoder(json_factory);
        request_handler = new RequestHandler(executor);
        channel_group_handler = new ServerChannelGroupHandler();
    }

    @Override
    public void initChannel(final SocketChannel channel) throws Exception {

        super.initChannel(channel);
        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("group", channel_group_handler);
        pipeline.addLast("encoder", request_decoder);
        pipeline.addLast("decoder", response_encoder);
        pipeline.addLast(RequestHandler.NAME, request_handler);
    }

}
