/*
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
