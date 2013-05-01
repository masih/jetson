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

import com.staticiser.jetson.exception.JsonRpcException;

public interface TestService {

    void doVoidWithNoParams() throws JsonRpcException;

    String saySomething() throws JsonRpcException;

    Integer say65535() throws JsonRpcException;

    Integer sayMinus65535() throws JsonRpcException;

    Boolean sayTrue() throws JsonRpcException;

    Boolean sayFalse() throws JsonRpcException;

    Boolean sayFalseOnRemote(Integer port) throws JsonRpcException;

    void throwException() throws Exception;

    void throwExceptionOnRemote(Integer port) throws Exception;

    Integer add(Integer a, Integer b) throws JsonRpcException;

    Integer addOnRemote(Integer a, Integer b, Integer port) throws JsonRpcException;

    TestObject getObject() throws JsonRpcException;

    TestObject getObjectOnRemote(Integer port) throws JsonRpcException;

    String concatinate(final String text, final Integer integer, final TestObject object, final char character) throws JsonRpcException;

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

        @Override
        public String toString() {

            return message;
        }
    }
}
