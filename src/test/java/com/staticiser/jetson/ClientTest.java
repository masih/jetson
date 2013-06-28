package com.staticiser.jetson;

import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.json.JsonClientFactory;
import java.net.InetSocketAddress;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ClientTest {

    private ClientFactory<TestService> client_factory;

    @Before
    public void setUp() throws Exception {

        client_factory = new JsonClientFactory<TestService>(TestService.class, AbstractTest.JSON_FACTORY);
    }

    @After
    public void tearDown() throws Exception {

        client_factory.shutdown();
    }

    @Test
    public void testConnectionRefusal() {

        final TestService service = client_factory.get(new InetSocketAddress(45689));
        try {
            service.saySomething();
            Assert.fail();
        }
        catch (final RPCException e) {
            //expected
        }
    }
}
