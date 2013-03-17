package uk.ac.standrews.cs.jetson.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;

public class DefaultServerSocketFactory extends ServerSocketFactory {

    @Override
    public ServerSocket createServerSocket(final int port) throws IOException {

        return new ServerSocket(port);
    }

    @Override
    public ServerSocket createServerSocket(final int port, final int backlog) throws IOException {

        return new ServerSocket(port, backlog);
    }

    @Override
    public ServerSocket createServerSocket(final int port, final int backlog, final InetAddress bind_address) throws IOException {

        return new ServerSocket(port, backlog, bind_address);
    }
}
