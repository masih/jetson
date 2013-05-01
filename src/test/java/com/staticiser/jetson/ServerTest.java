package com.staticiser.jetson;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Test;

import com.staticiser.jetson.exception.JsonRpcException;

public class ServerTest extends AbstractTest {

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

    @Override
    protected TestService getService() {

        return new NormalOperationTestService(CLIENT_FACTORY);
    }

}
