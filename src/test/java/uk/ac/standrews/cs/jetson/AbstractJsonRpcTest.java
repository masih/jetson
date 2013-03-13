package uk.ac.standrews.cs.jetson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractJsonRpcTest<TestService> {

    protected JsonRpcServer server;
    protected ServerSocket server_socket;
    protected InetSocketAddress server_address;
    protected JsonFactory json_factory;
    protected ExecutorService executor;
    protected TestService client;
    private JsonRpcProxyFactory proxy_factory;

    @Before
    public void setUp() throws Exception {

        initServerSocket();
        initJsonFactory();
        initExecutorService();
        server = new JsonRpcServer(getServiceType(), getService(), json_factory, executor);
        server.expose();
        server_address = server.getLocalSocketAddress();
        proxy_factory = new JsonRpcProxyFactory(getServiceType(), json_factory);
        client = proxy_factory.get(server_address);
    }

    protected abstract Class<TestService> getServiceType();

    protected abstract TestService getService();

    protected void initServerSocket() throws IOException {

        server_socket = new ServerSocket(0);
    }

    protected void initJsonFactory() {

        final ObjectMapper mapper = new ObjectMapper();
        json_factory = new JsonFactory(mapper);
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
