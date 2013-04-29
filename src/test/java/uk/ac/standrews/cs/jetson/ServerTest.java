package uk.ac.standrews.cs.jetson;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.standrews.cs.jetson.exception.JsonRpcException;

public class ServerTest extends AbstractTest<TestService> {

    @Test
    public void testExposure() throws IOException {

        client.saySomething();
        server.unexpose();
        try {
            client.saySomething();
            fail();
        }
        catch (final JsonRpcException e) {
            //expected
        }

        server.expose();
        client.saySomething();
        server.unexpose();
        try {
            client.saySomething();
            fail();
        }
        catch (final JsonRpcException e) {
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

    @Test
    public void testShutdown() throws JsonRpcException {

        client.saySomething();
        server.shutdown();
        try {
            client.saySomething();
            fail();
        }
        catch (final JsonRpcException e) {
            //expected
        }
    }

    @Override
    protected Class<TestService> getServiceType() {

        return TestService.class;
    }

    @Override
    protected TestService getService() {

        return new NormalOperationTestService(proxy_factory);
    }

}