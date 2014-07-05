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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mashti.jetson.exception.RPCException;

@RunWith(Parameterized.class)
public class ClientTest {

    private final ClientFactory<TestService> client_factory;

    @Parameterized.Parameters(name = "{index} -  client:{0}")
    public static Collection<Object[]> getParameters() {

        final Collection<Object[]> parameters = new ArrayList<Object[]>();
        parameters.add(new Object[] {AbstractTest.LEAN_CLIENT_FACTORY});
        parameters.add(new Object[] {AbstractTest.JSON_CLIENT_FACTORY});

        return parameters;
    }

    public ClientTest(final ClientFactory<TestService> client_factory) {

        this.client_factory = client_factory;
    }

    @Test(expected = ExecutionException.class)
    public void testConnectionRefusal() throws RPCException, ExecutionException, InterruptedException {

        final TestService service = client_factory.get(new InetSocketAddress(55555));
        service.saySomething().get();
        Assert.fail();
    }
}
