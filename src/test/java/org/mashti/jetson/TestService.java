/**
 * This file is part of jetson.
 *
 * jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jetson.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mashti.jetson;

import java.util.Collection;
import org.mashti.jetson.exception.RPCException;

public interface TestService {

    void doVoidWithNoParams() throws RPCException;

    int getNumberOfMessages(String... messages) throws RPCException;

    int getCollectionSize(Collection<String> collection) throws RPCException;

    String saySomething() throws RPCException;

    Integer say65535() throws RPCException;

    Integer sayMinus65535() throws RPCException;

    Boolean sayTrue() throws RPCException;

    Boolean sayFalse() throws RPCException;

    Boolean sayFalseOnRemote(Integer port) throws RPCException;

    void throwException() throws Exception;

    void throwExceptionOnRemote(Integer port) throws Exception;

    Integer add(Integer a, Integer b) throws RPCException;

    Integer addOnRemote(Integer a, Integer b, Integer port) throws RPCException;

    TestObject getObject() throws RPCException;

    TestObject getObjectOnRemote(Integer port) throws RPCException;

    String concatenate(final String text, final Integer integer, final TestObject object, final char character) throws RPCException;

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
