package uk.ac.standrews.cs.jetson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;

import uk.ac.standrews.cs.jetson.JsonRpcProxyFactory;
import uk.ac.standrews.cs.jetson.JsonRpcServer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractJsonRpcTest<TestService> {

    protected JsonRpcServer server;
    protected ServerSocket server_socket;
    protected InetSocketAddress server_address;
    protected JsonFactory json_factory;
    protected ExecutorService executor;
    protected TestService client;
    private final JsonRpcProxyFactory proxy_factory = new JsonRpcProxyFactory();

    @Before
    public void setUp() throws Exception {

        initServerSocket();
        initJsonFactory();
        initExecutorService();
        server = new JsonRpcServer(server_socket, getServiceType(), getService(), json_factory, executor);
        server.expose();
        server_address = new InetSocketAddress(server_socket.getLocalPort());
        client = proxy_factory.get(server_address, getServiceType(), json_factory);
    }

    protected abstract Class<TestService> getServiceType();

    protected abstract TestService getService();

    protected void initServerSocket() throws IOException {

        server_socket = new ServerSocket(0);
    }

    protected void initJsonFactory() {

        json_factory = new JsonFactory(new ObjectMapper());
    }

    protected void initExecutorService() {

        executor = Executors.newCachedThreadPool();
    }

    @After
    public void tearDown() throws Exception {

        server.unexpose();
        server.shutdown();
    }
}
