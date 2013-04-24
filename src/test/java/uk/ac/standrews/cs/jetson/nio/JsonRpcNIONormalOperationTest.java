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
package uk.ac.standrews.cs.jetson.nio;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.Test;

import uk.ac.standrews.cs.jetson.JsonRpcTestService;
import uk.ac.standrews.cs.jetson.NormalOperationTestService;
import uk.ac.standrews.cs.jetson.exception.JsonRpcException;

public class JsonRpcNIONormalOperationTest extends AbstractJsonRpcNIOTest<JsonRpcTestService> {

    @Override
    protected Class<JsonRpcTestService> getServiceType() {

        return JsonRpcTestService.class;
    }

    @Test
    public void testDoVoidWithNoParams() throws JsonRpcException {

        client.doVoidWithNoParams();
    }

    @Test
    public void testSaySomething() throws JsonRpcException {

        final String something = client.saySomething();
        Assert.assertEquals("something", something);
    }

    @Test
    public void testSay65535() throws JsonRpcException {

        final Integer _65535 = client.say65535();
        Assert.assertEquals(new Integer(65535), _65535);
    }

    @Test
    public void testSayMinus65535() throws JsonRpcException {

        final Integer minus65535 = client.sayMinus65535();
        Assert.assertEquals(new Integer(-65535), minus65535);
    }

    @Test
    public void testSayTrue() throws JsonRpcException {

        final Boolean _true = client.sayTrue();
        Assert.assertTrue(_true);
    }

    @Test
    public void testSayFalse() throws JsonRpcException {

        final Boolean _false = client.sayFalse();
        Assert.assertFalse(_false);
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
    public void testAdd() throws JsonRpcException {

        final Integer three = client.add(1, 2);
        Assert.assertEquals(new Integer(1 + 2), three);
        final Integer eleven = client.add(12, -1);
        Assert.assertEquals(new Integer(12 + -1), eleven);
        final Integer fifty_one = client.add(-61, 112);
        Assert.assertEquals(new Integer(-61 + 112), fifty_one);
        final Integer minus_seven = client.add(-4, -3);
        Assert.assertEquals(new Integer(-4 + -3), minus_seven);
    }

    @Test
    public void testGetObject() throws JsonRpcException {

        Assert.assertEquals(NormalOperationTestService.TEST_OBJECT_MESSAGE, client.getObject().getMessage());
    }

    @Test
    public void testSayFalseOnRemote() throws IOException {

        final JsonRpcServerNIO temp_server = new JsonRpcServerNIO(JsonRpcTestService.class, getService(), json_factory);
        temp_server.expose();
        try {
            final Boolean _false = client.sayFalseOnRemote(temp_server.getLocalSocketAddress().getPort());
            Assert.assertFalse(_false);
        }
        finally {
            temp_server.shutdown();
        }
    }

    @Test
    public void testConcurrentConnections() throws JsonRpcException, InterruptedException, ExecutionException {
        final ExecutorService executor = Executors.newCachedThreadPool();
        try {
            final CountDownLatch start_latch = new CountDownLatch(1);
            final List<Future<Void>> future_concurrent_tests = new ArrayList<Future<Void>>();
            for (int i = 0; i < 100; i++) {
                future_concurrent_tests.add(executor.submit(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {

                        start_latch.await();
                        testAdd();
                        testDoVoidWithNoParams();
                        testGetObject();
                        testSay65535();
                        testSayFalse();
                        testSayFalseOnRemote();
                        testSayMinus65535();
                        testSaySomething();
                        testSayTrue();
                        testThrowException();
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

    @Override
    protected JsonRpcTestService getService() {

        return new NormalOperationNIOTestService(proxy_factory);
    }

}
