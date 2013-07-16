package org.mashti.jetson;

import java.net.InetSocketAddress;
import java.util.Collection;
import org.mashti.jetson.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalOperationTestService implements TestService {

    public static final Exception TEST_EXCEPTION = new Exception("test exception");
    public static final String TEST_OBJECT_MESSAGE = "test message";
    private static final Logger LOGGER = LoggerFactory.getLogger(NormalOperationTestService.class);
    private final ClientFactory<TestService> proxy_factory;

    public NormalOperationTestService(final ClientFactory proxy_factory) {

        this.proxy_factory = proxy_factory;
    }

    @Override
    public void doVoidWithNoParams() {

        LOGGER.debug("doing nothing");
    }

    @Override
    public int getNumberOfMessages(final String... messages) {

        return messages != null ? messages.length : -1;
    }

    @Override
    public int getCollectionSize(final Collection<String> collection) throws RPCException {

        return collection != null ? collection.size() : -1;
    }

    @Override
    public String saySomething() {

        return "something";
    }

    @Override
    public Integer say65535() {

        return 65535;
    }

    @Override
    public Integer sayMinus65535() {

        return -65535;
    }

    @Override
    public Boolean sayTrue() {

        return true;
    }

    @Override
    public Boolean sayFalse() {

        return false;
    }

    @Override
    public Boolean sayFalseOnRemote(final Integer port) throws RPCException {

        final TestService remote_service = proxy_factory.get(new InetSocketAddress(port));
        return remote_service.sayFalse();
    }

    @Override
    public void throwException() throws Exception {

        throw TEST_EXCEPTION;
    }

    @Override
    public void throwExceptionOnRemote(final Integer port) throws Exception {

        final TestService remote_service = proxy_factory.get(new InetSocketAddress(port));
        remote_service.throwException();
    }

    @Override
    public Integer add(final Integer a, final Integer b) {

        return a + b;
    }

    @Override
    public Integer addOnRemote(final Integer a, final Integer b, final Integer port) throws RPCException {

        final TestService remote_service = proxy_factory.get(new InetSocketAddress(port));
        return remote_service.add(a, b);
    }

    @Override
    public TestObject getObject() {

        return new TestObject(TEST_OBJECT_MESSAGE);
    }

    @Override
    public TestObject getObjectOnRemote(final Integer port) throws RPCException {

        final TestService remote_service = proxy_factory.get(new InetSocketAddress(port));
        return remote_service.getObject();
    }

    @Override
    public String concatenate(final String text, final Integer integer, final TestObject object, final char character) throws RPCException {

        return text + integer + object + character;
    }
}
