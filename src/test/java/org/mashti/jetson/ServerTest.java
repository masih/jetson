/**
 * This file is part of jetson.
 *
 * jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jetson.  If not, see <http://www.gnu.org/licenses/>.
 */
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
