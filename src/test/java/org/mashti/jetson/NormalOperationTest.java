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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Test;
import org.mashti.jetson.TestService.TestObject;
import org.mashti.jetson.exception.RPCException;

import static org.junit.Assert.fail;

public class NormalOperationTest extends AbstractTest {

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    public NormalOperationTest(final ClientFactory<TestService> client_factory, final ServerFactory<TestService> server_factory) {

        super(client_factory, server_factory);
    }

    @Test
    public void testConcatenate() throws Exception {

        final String text = "some text";
        final int integer = 8852456;
        final TestObject object = new TestObject("X_1_23");
        final char character = '=';
        final String result = client.concatenate(text, integer, object, character).get();
        Assert.assertEquals(text + integer + object + character, result);
    }

    @Test
    public void testGetNumberOfMessages() throws Exception {

        Assert.assertEquals(Integer.valueOf(0), client.getNumberOfMessages(EMPTY_STRING_ARRAY).get());
        Assert.assertEquals(Integer.valueOf(0), client.getNumberOfMessages().get());
        Assert.assertEquals(Integer.valueOf(-1), client.getNumberOfMessages(null).get());
        Assert.assertEquals(Integer.valueOf(1), client.getNumberOfMessages("").get());
        Assert.assertEquals(Integer.valueOf(3), client.getNumberOfMessages("", null, "1").get());
    }

    @Test
    public void testGetCollectionSize() throws Exception {

        Assert.assertEquals(Integer.valueOf(0), client.getCollectionSize(new ArrayList<String>()).get());
        Assert.assertEquals(Integer.valueOf(-1), client.getCollectionSize(null).get());
        Assert.assertEquals(Integer.valueOf(1), client.getCollectionSize(Collections.singletonList((String) null)).get());
        Assert.assertEquals(Integer.valueOf(3), client.getCollectionSize(Arrays.asList("", null, "1")).get());
    }

    @Test
    public void testConcurrentClients() throws RPCException, InterruptedException, ExecutionException {

        final ExecutorService executor = Executors.newFixedThreadPool(500);
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
            executor.shutdownNow();
        }
    }

    @Test
    public void testGetObjectOnRemote() throws Exception {

        Assert.assertEquals(NormalOperationTestService.TEST_OBJECT_MESSAGE, client.getObjectOnRemote(temp_server_port).get().getMessage());
    }

    @Test
    public void testSayFalseOnRemote() throws Exception {

        final Boolean _false = client.sayFalseOnRemote(temp_server_port).get();
        Assert.assertFalse(_false);
    }

    @Test
    public void testGetObject() throws Exception {

        Assert.assertEquals(NormalOperationTestService.TEST_OBJECT_MESSAGE, client.getObject().get().getMessage());
    }

    @Test
    public void testAddOnRemote() throws Exception {

        testAddOnRemoteClient(client);
    }

    @Test
    public void testAdd() throws Exception {

        testAddOnClient(client);
    }

    @Test
    public void testThrowExceptionOnRemote() throws InterruptedException {

        try {
            client.throwExceptionOnRemote(temp_server_port).get();
            fail("expected exception");
        }
        catch (final ExecutionException e) {
            Assert.assertEquals(NormalOperationTestService.TEST_EXCEPTION.getClass(), e.getCause().getClass());
            Assert.assertEquals(NormalOperationTestService.TEST_EXCEPTION.getMessage(), e.getCause().getMessage());
        }
    }

    @Test
    public void testThrowException() throws InterruptedException {

        try {
            client.throwException().get();
            fail("expected exception");
        }
        catch (final ExecutionException e) {
            Assert.assertEquals(NormalOperationTestService.TEST_EXCEPTION.getClass(), e.getCause().getClass());
            Assert.assertEquals(NormalOperationTestService.TEST_EXCEPTION.getMessage(), e.getCause().getMessage());
        }
    }

    @Test
    public void testSayFalse() throws Exception {

        final Boolean _false = client.sayFalse().get();
        Assert.assertFalse(_false);
    }

    @Test
    public void testSayTrue() throws Exception {

        final Boolean _true = client.sayTrue().get();
        Assert.assertTrue(_true);
    }

    @Test
    public void testSayMinus65535() throws Exception {

        final Integer minus65535 = client.sayMinus65535().get();
        Assert.assertEquals(Integer.valueOf(-65535), minus65535);
    }

    @Test
    public void testSay65535() throws Exception {

        final Integer _65535 = client.say65535().get();
        Assert.assertEquals(Integer.valueOf(65535), _65535);
    }

    @Test
    public void testSaySomething() throws Exception {

        final String something = client.saySomething().get();
        Assert.assertEquals("something", something);
    }

    @Test
    public void testDoVoidWithNoParams() throws RPCException {

        client.doVoidWithNoParams();
    }

    @Test
    public void testConcurrentServers() throws RPCException, InterruptedException, ExecutionException {

        final ExecutorService executor = Executors.newFixedThreadPool(100);
        try {
            final CountDownLatch start_latch = new CountDownLatch(1);
            final List<Future<Void>> future_concurrent_tests = new ArrayList<Future<Void>>();
            for (int i = 0; i < 100; i++) {
                future_concurrent_tests.add(executor.submit(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {

                        start_latch.await();
                        final Server server = startTestServer();
                        final InetSocketAddress server_address = server.getLocalSocketAddress();
                        final TestService client = client_factory.get(server_address);

                        try {
                            testAddOnClient(client);
//                            testAddOnRemoteClient(client);
//                            testAddOnRemoteClient(client);
//                            testAddOnRemoteClient(client);
//                            testAddOnRemoteClient(client);
//                            testAddOnRemoteClient(client);
//                            testAddOnRemoteClient(client);
//                            testAddOnRemoteClient(client);
//                            testAddOnRemoteClient(client);
//                            testAddOnRemoteClient(client);
//                            testAddOnRemoteClient(client);
//                            testAddOnRemoteClient(client);
//                            testAddOnRemoteClient(client);
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
            executor.shutdownNow();
        }
    }

    @Override
    protected TestService getService() {

        return new NormalOperationTestService(client_factory);
    }

    private void testAddOnRemoteClient(final TestService client) throws Exception {

        final Integer three = client.addOnRemote(1, 2, temp_server_port).get();
        Assert.assertEquals(Integer.valueOf(1 + 2), three);
        final Integer eleven = client.addOnRemote(12, -1, temp_server_port).get();
        Assert.assertEquals(Integer.valueOf(12 - 1), eleven);
        final Integer fifty_one = client.addOnRemote(-61, 112, temp_server_port).get();
        Assert.assertEquals(Integer.valueOf(-61 + 112), fifty_one);
        final Integer minus_seven = client.addOnRemote(-4, -3, temp_server_port).get();
        Assert.assertEquals(Integer.valueOf(-4 - 3), minus_seven);
    }

    private static void testAddOnClient(final TestService client) throws Exception {

        final Integer three = client.add(1, 2).get();
        Assert.assertEquals(Integer.valueOf(1 + 2), three);
        final Integer eleven = client.add(12, -1).get();
        Assert.assertEquals(Integer.valueOf(12 - 1), eleven);
        final Integer fifty_one = client.add(-61, 112).get();
        Assert.assertEquals(Integer.valueOf(-61 + 112), fifty_one);
        final Integer minus_seven = client.add(-4, -3).get();
        Assert.assertEquals(Integer.valueOf(-4 - 3), minus_seven);
    }

}
