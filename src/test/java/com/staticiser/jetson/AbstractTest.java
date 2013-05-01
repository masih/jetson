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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.staticiser.jetson.ClientFactory;
import com.staticiser.jetson.Server;
import com.staticiser.jetson.ServerFactory;

public abstract class AbstractTest<TestService> {

    @Rule
    public Timeout global_timeout = new Timeout(10 * 60 * 1000);

    protected Server server;
    protected InetSocketAddress server_address;
    protected Server temp_server;
    protected int temp_server_port;
    protected JsonFactory json_factory;
    protected ExecutorService executor;
    protected TestService client;
    protected ClientFactory client_factory;
    protected ServerFactory server_factory;

    @Before
    public void setUp() throws Exception {

        initJsonFactory();
        client_factory = new ClientFactory(getServiceType(), json_factory);
        server_factory = new ServerFactory(getServiceType(), json_factory);
        server = startJsonRpcTestServer();
        server_address = server.getLocalSocketAddress();
        temp_server = startJsonRpcTestServer();
        temp_server_port = temp_server.getLocalSocketAddress().getPort();
        client = (TestService) client_factory.get(server_address);
    }

    protected Server startJsonRpcTestServer() throws IOException {

        final Server server = server_factory.createServer(getService());
        server.expose();
        return server;
    }

    protected abstract Class<TestService> getServiceType();

    protected abstract TestService getService();

    protected void initJsonFactory() {

        final ObjectMapper mapper = new ObjectMapper();
        json_factory = new JsonFactory(mapper);
    }

    @After
    public void tearDown() throws Exception {

        server.unexpose();
        temp_server.unexpose();

    }
}
