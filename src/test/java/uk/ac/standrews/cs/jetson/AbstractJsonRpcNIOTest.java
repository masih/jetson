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
package uk.ac.standrews.cs.jetson;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import org.junit.After;
import org.junit.Before;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractJsonRpcNIOTest<TestService> {

    protected JsonRpcServer server;
    protected InetSocketAddress server_address;
    protected JsonRpcServer temp_server;
    protected int temp_server_port;
    protected JsonFactory json_factory;
    protected ExecutorService executor;
    protected TestService client;
    protected JsonRpcProxyFactory proxy_factory;

    @Before
    public void setUp() throws Exception {

        initJsonFactory();
        proxy_factory = new JsonRpcProxyFactory(getServiceType(), json_factory);
        server = new JsonRpcServer(getServiceType(), getService(), json_factory);
        server.expose();
        server_address = server.getLocalSocketAddress();
        temp_server = new JsonRpcServer(getServiceType(), getService(), json_factory);
        temp_server.expose();
        temp_server_port = temp_server.getLocalSocketAddress().getPort();
        client = proxy_factory.get(server_address);
    }

    protected abstract Class<TestService> getServiceType();

    protected abstract TestService getService();

    protected void initJsonFactory() {

        final ObjectMapper mapper = new ObjectMapper();
        json_factory = new JsonFactory(mapper);
    }

    @After
    public void tearDown() throws Exception {

        server.shutdown();
        temp_server.shutdown();
    }
}
