package uk.ac.standrews.cs.jetson;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import uk.ac.standrews.cs.jetson.JsonRpcMessage;

public class JsonRpcMessageTest {

    JsonRpcMessage message;

    @Before
    public void setUp() throws Exception {

        message = new JsonRpcMessage() {
        };
    }

    @Test
    public void testDefaultVersion() {

        Assert.assertEquals(JsonRpcMessage.DEFAULT_VERSION, message.getVersion());
    }

}
