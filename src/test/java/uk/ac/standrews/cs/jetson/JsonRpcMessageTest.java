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
