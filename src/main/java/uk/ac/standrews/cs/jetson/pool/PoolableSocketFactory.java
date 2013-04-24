package uk.ac.standrews.cs.jetson.pool;

import java.net.Socket;

import javax.net.SocketFactory;

import org.apache.commons.pool.BasePoolableObjectFactory;

public class PoolableSocketFactory extends BasePoolableObjectFactory<Socket> {

    private final SocketFactory socket_factory;

    public PoolableSocketFactory(final SocketFactory socket_factory) {

        this.socket_factory = socket_factory;
    }

    @Override
    public Socket makeObject() throws Exception {

        return socket_factory.createSocket();
    }

    @Override
    public void destroyObject(final Socket socket) throws Exception {

        socket.close();
        super.destroyObject(socket);
    }

    @Override
    public boolean validateObject(final Socket socket) {

        return super.validateObject(socket);
    }

}
