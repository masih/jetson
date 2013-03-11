package uk.ac.standrews.cs.jetson;

import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.junit.Test;

import uk.ac.standrews.cs.jetson.exception.JsonRpcException;

public class JsonRpcNormalOperationTest extends AbstractJsonRpcTest<JsonRpcTestService> {

    private final class NormalOperationTestService implements JsonRpcTestService {

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

            //done;
        }

        @Override
        public Integer add(final Integer a, final Integer b) {

            return a + b;
        }
    }

    private static final Exception TEST_EXCEPTION = new Exception("test exception");
    private static final String TEST_OBJECT_MESSAGE = "test message";

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
            Assert.assertEquals(TEST_EXCEPTION.getClass(), e.getClass());
            Assert.assertEquals(TEST_EXCEPTION.getMessage(), e.getMessage());
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

        Assert.assertEquals(TEST_OBJECT_MESSAGE, client.getObject().getMessage());
    }

    @Override
    protected JsonRpcTestService getService() {

        return new NormalOperationTestService();
    }

}
