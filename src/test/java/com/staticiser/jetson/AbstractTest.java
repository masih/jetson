/*
 * Copyright 2013 Masih Hajiarabderkani
 *
 * This file is part of Jetson.
 *
 * Jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Jetson.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.staticiser.jetson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.staticiser.jetson.exception.RPCException;
import com.staticiser.jetson.json.JsonClientFactory;
import com.staticiser.jetson.json.JsonServerFactory;
import com.staticiser.jetson.lean.LeanClientFactory;
import com.staticiser.jetson.lean.LeanServerFactory;
import com.staticiser.jetson.lean.codec.Codec;
import com.staticiser.jetson.lean.codec.Codecs;
import com.staticiser.jetson.lean.codec.CollectionCodec;
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

@RunWith(Parameterized.class)
public abstract class AbstractTest {

    public static final Codecs CODECS = new Codecs();

    static {
        CODECS.register(new Codec() {

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

    protected static final ServerFactory<TestService> LEAN_SERVER_FACTORY = new LeanServerFactory<TestService>(TestService.class, CODECS);
    protected static final ClientFactory<TestService> LEAN_CLIENT_FACTORY = new LeanClientFactory<TestService>(TestService.class, CODECS);
    protected static final JsonFactory JSON_FACTORY = new JsonFactory(new ObjectMapper());
    protected static final ServerFactory<TestService> JSON_SERVER_FACTORY = new JsonServerFactory<TestService>(TestService.class, JSON_FACTORY);
    protected static final ClientFactory<TestService> JSON_CLIENT_FACTORY = new JsonClientFactory<TestService>(TestService.class, JSON_FACTORY);
    protected final ClientFactory<TestService> client_factory;
    protected final ServerFactory<TestService> server_factory;
    @Rule
    public Timeout global_timeout = new Timeout(10 * 60 * 1000);
    protected Server server;
    protected InetSocketAddress server_address;
    protected Server temp_server;
    protected int temp_server_port;
    protected ExecutorService executor;
    protected TestService client;

    protected AbstractTest(ClientFactory<TestService> client_factory, ServerFactory<TestService> server_factory) {
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

        server = startJsonRpcTestServer();
        server_address = server.getLocalSocketAddress();
        temp_server = startJsonRpcTestServer();
        temp_server_port = temp_server.getLocalSocketAddress().getPort();
        client = client_factory.get(server_address);
    }

    @After
    public void tearDown() throws Exception {

        server.unexpose();
        temp_server.unexpose();
    }

    protected Server startJsonRpcTestServer() throws IOException {

        final Server server = server_factory.createServer(getService());
        server.expose();
        return server;
    }

    protected abstract TestService getService();
}
