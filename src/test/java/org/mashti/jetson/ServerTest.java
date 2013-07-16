package org.mashti.jetson;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.junit.Assert;
import org.junit.Test;
import org.mashti.jetson.exception.RPCException;

import static org.junit.Assert.fail;

public class ServerTest extends AbstractTest {

    public ServerTest(final ClientFactory<TestService> client_factory, final ServerFactory<TestService> server_factory) {

        super(client_factory, server_factory);
    }

    @Test
    public void testExposure() throws IOException {

        client.saySomething();
        server.unexpose();
        try {
            client.saySomething();
            fail();
        }
        catch (final RPCException e) {
            //expected
        }

        server.expose();
        client.saySomething();
        server.unexpose();
        try {
            client.saySomething();
            fail();
        }
        catch (final RPCException e) {
            //expected
        }
    }

    @Test
    public void testIsExposed() throws IOException {

        server.unexpose();
        Assert.assertFalse(server.isExposed());
        server.expose();
        Assert.assertTrue(server.isExposed());
    }

    @Test
    public void testGetLocalSocketAddress() throws IOException {

        server.unexpose();
        final InetSocketAddress address = new InetSocketAddress("localhost", 58852);
        server.setBindAddress(address);
        server.expose();
        Assert.assertEquals(address, server.getLocalSocketAddress());
    }

    @Override
    protected TestService getService() {

        return new NormalOperationTestService(client_factory);
    }

}
