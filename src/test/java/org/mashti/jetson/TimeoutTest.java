/**
 * Copyright © 2015, Masih H. Derkani
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
