package org.mashti.jetson;

import io.netty.channel.ChannelHandlerContext;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class FaultyServerFactory<Service> extends ServerFactory<Service> {

    /** Instantiates a new server factory. */
    protected FaultyServerFactory(final ServerChannelInitializer handler) {
        super(handler);
    }

    @Override
    public Server createServer(Service service) {
        return new Server(server_bootstrap, service, request_executor) {

            @Override
            public void handle(final ChannelHandlerContext context, final FutureResponse future_response) {
                // do nothing
            }
        };
    }
}
