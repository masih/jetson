/**
 * This file is part of jetson.
 *
 * jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jetson.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mashti.jetson;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ServerChannelInitializer extends BaseChannelInitializer {

    private final RequestDecoder request_decoder;
    private final ResponseEncoder response_encoder;
    private final RequestHandler request_handler;

    public ServerChannelInitializer(final RequestDecoder request_decoder, final ResponseEncoder response_encoder) {

        this.request_decoder = request_decoder;
        this.response_encoder = response_encoder;
        request_handler = new RequestHandler();
    }

    @Override
    public void initChannel(final SocketChannel channel) throws Exception {

        super.initChannel(channel);
        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("decoder", request_decoder);
        pipeline.addLast("encoder", response_encoder);
        pipeline.addLast(RequestHandler.NAME, request_handler);
    }

}
