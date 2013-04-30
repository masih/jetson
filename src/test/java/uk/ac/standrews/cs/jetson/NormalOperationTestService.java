package uk.ac.standrews.cs.jetson;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.standrews.cs.jetson.exception.JsonRpcException;

public class NormalOperationTestService implements TestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NormalOperationTestService.class);
    public static final Exception TEST_EXCEPTION = new Exception("test exception");
    public static final String TEST_OBJECT_MESSAGE = "test message";
    private final ClientFactory proxy_factory;

    public NormalOperationTestService(final ClientFactory proxy_factory) {

        this.proxy_factory = proxy_factory;
    }

    @Override
    public void throwException() throws Exception {

        throw TEST_EXCEPTION;
    }

    @Override
    public Boolean sayTrue() {

        return true;
    }

    @Override
    public String saySomething() {

        return "something";
    }

    @Override
    public Integer sayMinus65535() {

        return -65535;
    }

    @Override
    public Boolean sayFalse() {

        return false;
    }

    @Override
    public Integer say65535() {

        return 65535;
    }

    @Override
    public TestObject getObject() {

        return new TestObject(TEST_OBJECT_MESSAGE);
    }

    @Override
    public void doVoidWithNoParams() {

        LOGGER.debug("doing nothing");
    }

    @Override
    public Integer add(final Integer a, final Integer b) {

        return a + b;
    }

    @Override
    public Boolean sayFalseOnRemote(final Integer port) throws JsonRpcException {

        final TestService remote_service = (TestService) proxy_factory.get(new InetSocketAddress(port));
        return remote_service.sayFalse();
    }

    @Override
    public void throwExceptionOnRemote(final Integer port) throws Exception {

        final TestService remote_service = (TestService) proxy_factory.get(new InetSocketAddress(port));
        remote_service.throwException();
    }

    @Override
    public Integer addOnRemote(final Integer a, final Integer b, final Integer port) throws JsonRpcException {

        final TestService remote_service = (TestService) proxy_factory.get(new InetSocketAddress(port));
        return remote_service.add(a, b);
    }

    @Override
    public TestObject getObjectOnRemote(final Integer port) throws JsonRpcException {

        final TestService remote_service = (TestService) proxy_factory.get(new InetSocketAddress(port));
        return remote_service.getObject();
    }

    @Override
    public String concatinate(final String text, final Integer integer, final TestObject object, final char character) throws JsonRpcException {

        return text + integer + object + character;
    }
}
