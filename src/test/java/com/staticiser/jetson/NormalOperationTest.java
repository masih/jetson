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

import com.staticiser.jetson.TestService.TestObject;
import com.staticiser.jetson.exception.RPCException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

public class NormalOperationTest extends AbstractTest {

    public NormalOperationTest(final ClientFactory<TestService> client_factory, final ServerFactory<TestService> server_factory) {
        super(client_factory, server_factory);
    }

    @Test
    public void testConcatenate() throws RPCException {

        final String text = "some text";
        final int integer = 8852456;
        final TestObject object = new TestObject("X_1_23");
        final char character = '=';
        final String result = client.concatenate(text, integer, object, character);
        Assert.assertEquals(text + integer + object + character, result);
    }

    @Test
    public void testGetNumberOfMessages() throws Exception {

        Assert.assertEquals(0, client.getNumberOfMessages(new String[0]));
        Assert.assertEquals(0, client.getNumberOfMessages());
        Assert.assertEquals(-1, client.getNumberOfMessages(null));
        Assert.assertEquals(1, client.getNumberOfMessages(""));
        Assert.assertEquals(3, client.getNumberOfMessages("", null, "1"));
        Assert.assertEquals(3, client.getNumberOfMessages(new String[] {"", null, "1"}));
    }

    @Test
    public void testGetCollectionSize() throws Exception {
        Assert.assertEquals(0, client.getCollectionSize(new ArrayList()));
        Assert.assertEquals(-1, client.getCollectionSize(null));
        Assert.assertEquals(1, client.getCollectionSize(Arrays.asList((String) null)));
        Assert.assertEquals(3, client.getCollectionSize(Arrays.asList("", null, "1")));
    }

    @Test
    public void testConcurrentClients() throws RPCException, InterruptedException, ExecutionException {

        final ExecutorService executor = Executors.newFixedThreadPool(100);
        try {
            final CountDownLatch start_latch = new CountDownLatch(1);
            final List<Future<Void>> future_concurrent_tests = new ArrayList<Future<Void>>();
            for (int i = 0; i < 500; i++) {
                future_concurrent_tests.add(executor.submit(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {

                        start_latch.await();
                        testAdd();
                        testAddOnRemote();
                        testDoVoidWithNoParams();
                        testGetObject();
                        testGetObjectOnRemote();
                        testSay65535();
                        testSayFalse();
                        testSayFalseOnRemote();
                        testSayMinus65535();
                        testSaySomething();
                        testSayTrue();
                        testThrowException();
                        testThrowExceptionOnRemote();
                        return null;
                    }
                }));
            }
            start_latch.countDown();
            for (final Future<Void> f : future_concurrent_tests) {
                f.get();
            }
        }
        finally {
            executor.shutdown();
        }
    }

    @Test
    public void testGetObjectOnRemote() throws IOException {

        Assert.assertEquals(NormalOperationTestService.TEST_OBJECT_MESSAGE, client.getObjectOnRemote(temp_server_port).getMessage());
    }

    @Test
    public void testSayFalseOnRemote() throws IOException {

        final Boolean _false = client.sayFalseOnRemote(temp_server_port);
        Assert.assertFalse(_false);
    }

    @Test
    public void testGetObject() throws RPCException {

        Assert.assertEquals(NormalOperationTestService.TEST_OBJECT_MESSAGE, client.getObject().getMessage());
    }

    @Test
    public void testAddOnRemote() throws RPCException {

        testAddOnRemoteClient(client);
    }

    @Test
    public void testAdd() throws RPCException {

        testAddOnClient(client);
    }

    @Test
    public void testThrowExceptionOnRemote() {

        try {
            client.throwExceptionOnRemote(temp_server_port);
            fail("expected exception");
        }
        catch (final Exception e) {
            Assert.assertEquals(NormalOperationTestService.TEST_EXCEPTION.getClass(), e.getClass());
            Assert.assertEquals(NormalOperationTestService.TEST_EXCEPTION.getMessage(), e.getMessage());
        }
    }

    @Test
    public void testThrowException() {

        try {
            client.throwException();
            fail("expected exception");
        }
        catch (final Exception e) {
            Assert.assertEquals(NormalOperationTestService.TEST_EXCEPTION.getClass(), e.getClass());
            Assert.assertEquals(NormalOperationTestService.TEST_EXCEPTION.getMessage(), e.getMessage());
        }
    }

    @Test
    public void testSayFalse() throws RPCException {

        final Boolean _false = client.sayFalse();
        Assert.assertFalse(_false);
    }

    @Test
    public void testSayTrue() throws RPCException {

        final Boolean _true = client.sayTrue();
        Assert.assertTrue(_true);
    }

    @Test
    public void testSayMinus65535() throws RPCException {

        final Integer minus65535 = client.sayMinus65535();
        Assert.assertEquals(new Integer(-65535), minus65535);
    }

    @Test
    public void testSay65535() throws RPCException {

        final Integer _65535 = client.say65535();
        Assert.assertEquals(new Integer(65535), _65535);
    }

    @Test
    public void testSaySomething() throws RPCException {

        final String something = client.saySomething();
        Assert.assertEquals("something", something);
    }

    @Test
    public void testDoVoidWithNoParams() throws RPCException {

        client.doVoidWithNoParams();
    }

    @Test
    //    @Ignore
    public void testConcurrentServers() throws RPCException, InterruptedException, ExecutionException {

        final ExecutorService executor = Executors.newFixedThreadPool(100);
        try {
            final CountDownLatch start_latch = new CountDownLatch(1);
            final List<Future<Void>> future_concurrent_tests = new ArrayList<Future<Void>>();
            for (int i = 0; i < 500; i++) {
                future_concurrent_tests.add(executor.submit(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {

                        start_latch.await();
                        final Server server = startJsonRpcTestServer();
                        final InetSocketAddress server_address = server.getLocalSocketAddress();

                        final TestService client = client_factory.get(server_address);

                        try {
                            testAddOnClient(client);
                            testAddOnRemoteClient(client);
                        }
                        finally {
                            server.unexpose();
                        }
                        return null;
                    }
                }));
            }
            start_latch.countDown();
            for (final Future<Void> f : future_concurrent_tests) {
                f.get();
            }
        }
        finally {
            executor.shutdown();
        }
    }

    private void testAddOnRemoteClient(final TestService client) throws RPCException {

        final Integer three = client.addOnRemote(1, 2, temp_server_port);
        Assert.assertEquals(new Integer(1 + 2), three);
        final Integer eleven = client.addOnRemote(12, -1, temp_server_port);
        Assert.assertEquals(new Integer(12 + -1), eleven);
        final Integer fifty_one = client.addOnRemote(-61, 112, temp_server_port);
        Assert.assertEquals(new Integer(-61 + 112), fifty_one);
        final Integer minus_seven = client.addOnRemote(-4, -3, temp_server_port);
        Assert.assertEquals(new Integer(-4 + -3), minus_seven);
    }

    private void testAddOnClient(final TestService client) throws RPCException {

        //TODO test null
        final Integer three = client.add(1, 2);
        Assert.assertEquals(new Integer(1 + 2), three);
        final Integer eleven = client.add(12, -1);
        Assert.assertEquals(new Integer(12 + -1), eleven);
        final Integer fifty_one = client.add(-61, 112);
        Assert.assertEquals(new Integer(-61 + 112), fifty_one);
        final Integer minus_seven = client.add(-4, -3);
        Assert.assertEquals(new Integer(-4 + -3), minus_seven);
    }

    @Override
    protected TestService getService() {

        return new NormalOperationTestService(client_factory);
    }

}
