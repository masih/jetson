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

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalOperationTestService implements TestService {

    public static final Exception TEST_EXCEPTION = new Exception("test exception");
    public static final String TEST_OBJECT_MESSAGE = "test message";
    private static final Logger LOGGER = LoggerFactory.getLogger(NormalOperationTestService.class);
    private final ClientFactory<TestService> proxy_factory;

    public NormalOperationTestService(final ClientFactory<TestService> proxy_factory) {

        this.proxy_factory = proxy_factory;
    }

    @Override
    public CompletableFuture<Void> doVoidWithNoParams() {

        return CompletableFuture.runAsync(() -> {LOGGER.debug("doing nothing");});
    }

    @Override
    public CompletableFuture<Integer> getNumberOfMessages(final String... messages) {

        return CompletableFuture.supplyAsync(() -> {return messages != null ? messages.length : -1;});
    }

    @Override
    public CompletableFuture<Integer> getCollectionSize(final Collection<String> collection) {

        return CompletableFuture.supplyAsync(() -> {
            return collection != null ? collection.size() : -1;
        });
    }

    @Override
    public CompletableFuture<String> saySomething() {

        return CompletableFuture.supplyAsync(() -> {return "something";});
    }

    @Override
    public CompletableFuture<Integer> say65535() {

        return CompletableFuture.completedFuture(65535);
    }

    @Override
    public CompletableFuture<Integer> sayMinus65535() {

        return CompletableFuture.completedFuture(-65535);
    }

    @Override
    public CompletableFuture<Boolean> sayTrue() {

        return CompletableFuture.completedFuture(Boolean.TRUE);
    }

    @Override
    public CompletableFuture<Boolean> sayFalse() {

        return CompletableFuture.completedFuture(Boolean.FALSE);
    }

    @Override
    public CompletableFuture<Void> sleepForFiveSeconds() {

        CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {

            try {
                Thread.sleep(5000);
                future.complete(null);
            }
            catch (InterruptedException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Boolean> sayFalseOnRemote(final Integer port) {

        final TestService remote_service = proxy_factory.get(new InetSocketAddress("localhost", port));
        return remote_service.sayFalse();
    }

    @Override
    public CompletableFuture<Void> throwException() {

        final CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(TEST_EXCEPTION);
        return future;
    }

    @Override
    public CompletableFuture<Void> throwExceptionOnRemote(final Integer port) {

        final TestService remote_service = proxy_factory.get(new InetSocketAddress("localhost", port));
        return remote_service.throwException();
    }

    @Override
    public CompletableFuture<Integer> add(final Integer a, final Integer b) {

        return CompletableFuture.supplyAsync(() -> {return a + b;});
    }

    @Override
    public CompletableFuture<Integer> addOnRemote(final Integer a, final Integer b, final Integer port) {

        final TestService remote_service = proxy_factory.get(new InetSocketAddress("localhost", port));
        return remote_service.add(a, b);
    }

    @Override
    public CompletableFuture<TestObject> getObject() {

        return CompletableFuture.supplyAsync(() -> { return new TestObject(TEST_OBJECT_MESSAGE);});
    }

    @Override
    public CompletableFuture<TestObject> getObjectOnRemote(final Integer port) {

        final TestService remote_service = proxy_factory.get(new InetSocketAddress("localhost", port));
        return remote_service.getObject();
    }

    @Override
    public CompletableFuture<String> concatenate(final String text, final Integer integer, final TestObject object, final char character) {

        return CompletableFuture.supplyAsync(() -> {return text + integer + object + character;});
    }
}
