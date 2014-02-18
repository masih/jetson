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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
class RequestHandler extends ChannelInboundHandlerAdapter {

    static final String NAME = "request_handler";
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    @Override
    public void channelActive(final ChannelHandlerContext context) throws Exception {

        final Channel channel = context.channel();
        final Server server = Server.getServerFromContext(context);
        server.notifyChannelActivation(channel);
        super.channelActive(context);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext context) throws Exception {

        final Channel channel = context.channel();
        final Server server = Server.getServerFromContext(context);
        server.notifyChannelInactivation(channel);
        super.channelInactive(context);
    }

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) throws Exception {

        final Server server = Server.getServerFromContext(context);
        server.handle(context, (FutureResponse) message);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {

        LOGGER.trace("caught on server handler", cause);
        ChannelUtils.notifyCaughtException(context.channel(), cause);
        context.close();
    }
}
