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

import java.io.IOException;
import java.net.InetSocketAddress;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

public class ServerTest extends AbstractTest {

    public ServerTest(final ClientFactory<TestService> client_factory, final ServerFactory<TestService> server_factory) {

        super(client_factory, server_factory);
    }

    @Test
    public void testExposure() throws IOException, InterruptedException {

        client.saySomething();
        server.unexpose();
        try {
            client.saySomething().get();
            fail();
        }
        catch (final Exception e) {
            //expected
        }

        server.expose();
        Thread.sleep(5000);
        client.saySomething();
        server.unexpose();
        try {
            client.saySomething().get();
            fail();
        }
        catch (final Exception e) {
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
