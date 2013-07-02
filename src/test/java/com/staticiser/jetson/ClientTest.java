package com.staticiser.jetson;

import com.staticiser.jetson.exception.RPCException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ClientTest {

    protected final ClientFactory<TestService> client_factory;

    @Parameterized.Parameters(name = "{index} -  client:{0}")
    public static Collection<Object[]> getParameters() {

        final Collection<Object[]> parameters = new ArrayList<Object[]>();
        parameters.add(new Object[]{AbstractTest.LEAN_CLIENT_FACTORY});
        parameters.add(new Object[]{AbstractTest.JSON_CLIENT_FACTORY});

        return parameters;
    }

    public ClientTest(final ClientFactory<TestService> client_factory) {

        this.client_factory = client_factory;
    }

    @Test(expected = RPCException.class)
    public void testConnectionRefusal() throws RPCException {

        final TestService service = client_factory.get(new InetSocketAddress(55555));
        service.saySomething();
        Assert.fail();
    }
}
