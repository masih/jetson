package uk.ac.standrews.cs.jetson.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public class DefaultSocketFactory extends SocketFactory {

    @Override
    public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {

        return new Socket(host, port);
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {

        return new Socket(host, port);
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress local_host, final int local_port) throws IOException, UnknownHostException {

        return new Socket(host, port, local_host, local_port);
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress local_address, final int local_port) throws IOException {

        return new Socket(address, port, local_address, local_port);
    }
}
