package uk.ac.standrews.cs.jetson;

import uk.ac.standrews.cs.jetson.exception.JsonRpcException;

public interface JsonRpcTestService {

    void doVoidWithNoParams() throws JsonRpcException;

    String saySomething() throws JsonRpcException;

    Integer say65535() throws JsonRpcException;

    Integer sayMinus65535() throws JsonRpcException;

    Boolean sayTrue() throws JsonRpcException;

    Boolean sayFalse() throws JsonRpcException;

    void throwException() throws Exception;

    Integer add(Integer a, Integer b) throws JsonRpcException;

    TestObject getObject() throws JsonRpcException;

    public class TestObject {

        private String message;

        public TestObject() {

        }

        public TestObject(final String message) {

            this.message = message;
        }

        public String getMessage() {

            return message;
        }

        public void setMessage(final String message) {

            this.message = message;
        }
    }
}
