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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.json.JsonClientFactory;
import org.mashti.jetson.json.JsonServerFactory;
import org.mashti.jetson.lean.LeanClientFactory;
import org.mashti.jetson.lean.LeanServerFactory;
import org.mashti.jetson.lean.codec.Codec;
import org.mashti.jetson.lean.codec.Codecs;
import org.mashti.jetson.lean.codec.CollectionCodec;

@RunWith(Parameterized.class)
public abstract class AbstractTest {

    private static final Codecs CODECS = new Codecs();
    private static final JsonFactory JSON_FACTORY = new JsonFactory(new ObjectMapper());
    static final ClientFactory<TestService> LEAN_CLIENT_FACTORY = new LeanClientFactory<TestService>(TestService.class, CODECS);
    static final ClientFactory<TestService> JSON_CLIENT_FACTORY = new JsonClientFactory<TestService>(TestService.class, JSON_FACTORY);
    private static final ServerFactory<TestService> LEAN_SERVER_FACTORY = new LeanServerFactory<TestService>(TestService.class, CODECS);
    private static final ServerFactory<TestService> JSON_SERVER_FACTORY = new JsonServerFactory<TestService>(TestService.class, JSON_FACTORY);

    static {
        CODECS.register(0, new Codec() {

            @Override
            public boolean isSupported(final Type type) {

                return type != null && type instanceof Class<?> && TestService.TestObject.class.isAssignableFrom((Class<?>) type);
            }

            @Override
            public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

                final TestService.TestObject testObject = (TestService.TestObject) value;
                codecs.encodeAs(testObject.getMessage(), out, String.class);
            }

            @Override
            public TestService.TestObject decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

                final String message = codecs.decodeAs(in, String.class);
                return new TestService.TestObject(message);
            }
        });
        CODECS.register(new CollectionCodec() {

            @Override
            protected Collection constructCollectionOfType(final Type type) {

                return new ArrayList();
            }
        });
    }

    final ClientFactory<TestService> client_factory;
    private final ServerFactory<TestService> server_factory;
    @Rule
    public Timeout global_timeout = new Timeout(10 * 60 * 1000);
    protected ExecutorService executor;
    Server server;
    int temp_server_port;
    TestService client;
    private InetSocketAddress server_address;
    private Server temp_server;

    protected AbstractTest(final ClientFactory<TestService> client_factory, final ServerFactory<TestService> server_factory) {

        this.client_factory = client_factory;
        this.server_factory = server_factory;
    }

    @Parameterized.Parameters(name = "{index} -  client:{0}, server: {1}")
    public static Collection<Object[]> getParameters() {

        final Collection<Object[]> parameters = new ArrayList<Object[]>();
        parameters.add(new Object[] {LEAN_CLIENT_FACTORY, LEAN_SERVER_FACTORY});
        parameters.add(new Object[] {JSON_CLIENT_FACTORY, JSON_SERVER_FACTORY});

        return parameters;
    }

    @Before
    public void setUp() throws Exception {

        server = startTestServer();
        server_address = server.getLocalSocketAddress();
        temp_server = startTestServer();
        temp_server_port = temp_server.getLocalSocketAddress().getPort();
        client = client_factory.get(server_address);
    }

    @After
    public void tearDown() throws Exception {

        server.unexpose();
        temp_server.unexpose();
    }

    Server startTestServer() throws IOException {

        final Server server = server_factory.createServer(getService());
        server.setBindAddress(new InetSocketAddress("localhost", 0));
        server.expose();
        return server;
    }

    protected abstract TestService getService();
}
