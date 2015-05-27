/**
 * Copyright © 2015, Masih H. Derkani
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
        ChannelFuturePool.notifyCaughtException(context.channel(), cause);
        context.close();
    }
}
