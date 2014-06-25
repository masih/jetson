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

import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mashti.jetson.exception.RPCException;
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

    @Test(expected = ExecutionException.class)
    public void testConnectionTimeout() throws RPCException, ExecutionException, InterruptedException {

        final TestService service = client_factory.get(server.getLocalSocketAddress());
        service.saySomething().get();
        Assert.fail();
    }

    @After
    public void tearDown() throws Exception {

        server.unexpose();
    }
}
