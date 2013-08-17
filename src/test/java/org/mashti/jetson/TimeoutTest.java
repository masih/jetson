package org.mashti.jetson;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.TransportException;
import org.mashti.jetson.lean.LeanClientFactory;
import org.mashti.jetson.lean.LeanServerChannelInitializer;
import org.mashti.jetson.lean.codec.Codecs;

public class TimeoutTest {

    private LeanClientFactory<TestService> client_factory;
    private Server server;
    private FaultyServerFactory<TestService> faultyServerFactory;

    @Before
    public void setUp() throws Exception {

        faultyServerFactory = new FaultyServerFactory<TestService>(new LeanServerChannelInitializer<TestService>(TestService.class, new Codecs()));
        client_factory = new LeanClientFactory<TestService>(TestService.class);
        server = faultyServerFactory.createServer(new NormalOperationTestService(client_factory));
        server.expose();

    }

    @Test(expected = TransportException.class)
    public void testConnectionRefusal() throws RPCException {

        final TestService service = client_factory.get(server.getLocalSocketAddress());
        service.saySomething();
        Assert.fail();
    }

    @After
    public void tearDown() throws Exception {
        server.unexpose();
    }
}
