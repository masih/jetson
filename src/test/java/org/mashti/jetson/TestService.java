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
import java.util.concurrent.CompletableFuture;

public interface TestService {

    CompletableFuture<Void> doVoidWithNoParams();

    CompletableFuture<Integer> getNumberOfMessages(String... messages);

    CompletableFuture<Integer> getCollectionSize(Collection<String> collection);

    CompletableFuture<String> saySomething();

    CompletableFuture<Integer> say65535();

    CompletableFuture<Integer> sayMinus65535();

    CompletableFuture<Boolean> sayTrue();

    CompletableFuture<Boolean> sayFalse();

    CompletableFuture<Void> sleepForFiveSeconds();

    CompletableFuture<Boolean> sayFalseOnRemote(Integer port);

    CompletableFuture<Void> throwException();

    CompletableFuture<Void> throwExceptionOnRemote(Integer port);

    CompletableFuture<Integer> add(Integer a, Integer b);

    CompletableFuture<Integer> addOnRemote(Integer a, Integer b, Integer port);

    CompletableFuture<TestObject> getObject();

    CompletableFuture<TestObject> getObjectOnRemote(Integer port);

    CompletableFuture<String> concatenate(String text, Integer integer, TestObject object, char character);

    class TestObject {

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
