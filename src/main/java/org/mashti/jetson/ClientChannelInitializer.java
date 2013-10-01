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

public class ClientChannelInitializer extends BaseChannelInitializer {

    private final RequestEncoder request_encoder;
    private final ResponseDecoder response_decoder;
    private final ResponseHandler client_handler;

    public ClientChannelInitializer(final RequestEncoder request_encoder, final ResponseDecoder response_decoder) {

        this.request_encoder = request_encoder;
        this.response_decoder = response_decoder;
        client_handler = new ResponseHandler();
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
