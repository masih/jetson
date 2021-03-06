/**
 * Copyright © 2015, Masih H. Derkani
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mashti.jetson.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * A utility class for parsing a JSON stream using {@link JsonParser}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class JsonParserUtil {

    private JsonParserUtil() {

    }

    /**
     * Consumes a field name from a JSON stream and checks that the filed name matches one of the given {@code filed_names}.
     * Throws {@link JsonParseException} if the consumed field name does not match any of the given {@code field_names}.
     *
     * @param parser the parser to read from
     * @param field_names the field names to match
     * @return the matched field name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String expectFieldNames(final JsonParser parser, final String... field_names) throws IOException {

        final String next_field_name = nextFieldName(parser);
        for (final String field_name : field_names) {
            if (next_field_name.equals(field_name)) { return next_field_name; }
        }
        throw new JsonParseException("expected one of field names " + Arrays.toString(field_names), parser.getCurrentLocation());
    }

    /**
     * Consumes a field name from a JSON stream and checks that the filed name matches the given {@code filed_name}.
     * Throws {@link JsonParseException} if the consumed field name does not match the given {@code field_name}.
     *
     * @param parser the parser to read from
     * @param field_name the field name to expect
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void expectFieldName(final JsonParser parser, final String field_name) throws IOException {

        if (!nextFieldName(parser).equals(field_name)) { throw new JsonParseException("expected the field name " + field_name, parser.getCurrentLocation()); }
    }

    /**
     * Consumes the next token from a JSON stream and checks that the token is a {@link JsonToken#FIELD_NAME}.
     * Throws {@link JsonParseException} if the token is not a {@link JsonToken#FIELD_NAME}.
     *
     * @param parser the parser to read from
     * @return the field name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static String nextFieldName(final JsonParser parser) throws IOException {

        if (parser.nextToken() == JsonToken.FIELD_NAME) { return parser.getCurrentName(); }
        throw new JsonParseException("expected some field name", parser.getCurrentLocation());
    }

    /**
     * Reads a field and its value as the given {@code expected_type}.
     *
     * @param <T> the generic type
     * @param parser the parser
     * @param expected_filed_name the expected filed name
     * @param expected_type the expected type of the field value
     * @return the value
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static <T> T readFieldValueAs(final JsonParser parser, final String expected_filed_name, final Class<T> expected_type) throws IOException {

        expectFieldName(parser, expected_filed_name);
        return readValueAs(parser, expected_type);
    }

    /**
     * Reads the values of a JSON array as the proved types.
     *
     * @param parser the parser
     * @param expected_types the expected types of array values
     * @return the array values
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Object[] readArrayValuesAs(final JsonParser parser, final Type[] expected_types) throws IOException {

        expectStartArray(parser);
        final int expected_values_length = expected_types.length;
        final Object[] params;
        if (expected_values_length != 0) {
            params = new Object[expected_values_length];
            for (int i = 0; i < expected_values_length; i++) {
                params[i] = readValueAs(parser, expected_types[i]);
            }
        }
        else {
            params = null;
        }
        expectEndArray(parser);
        return params;
    }

    /**
     * Expects start array.
     *
     * @param parser the parser
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void expectStartArray(final JsonParser parser) throws IOException {

        if (parser.nextToken() != JsonToken.START_ARRAY) { throw new JsonParseException("expected start array", parser.getCurrentLocation()); }
    }

    /**
     * Consumes the next token from a JSON stream and checks that the token is a {@link JsonToken#START_ARRAY}.
     * Throws {@link JsonParseException} if the token is not a {@link JsonToken#START_ARRAY}.
     *
     * @param parser the parser to read from
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void expectEndArray(final JsonParser parser) throws IOException {

        if (parser.nextToken() != JsonToken.END_ARRAY) { throw new JsonParseException("expected end array", parser.getCurrentLocation()); }
    }

    /**
     * Reads a field value as the provided type. This method supports parametrised types.
     *
     * @param parser the parser
     * @param expected_type the expected type of the value
     * @return the value
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Object readValueAs(final JsonParser parser, final Type expected_type) throws IOException {

        final Object value;
        if (expected_type.equals(Void.TYPE)) {
            expectNullValue(parser);
            value = null;
        }
        else {
            parser.nextToken();
            value = parser.readValueAs(toTypeReference(expected_type));
        }
        return value;
    }

    /**
     * Consumes the next token from a JSON stream and checks that the token is a {@link JsonToken#VALUE_NULL}.
     * Throws {@link JsonParseException} if the token is not a {@link JsonToken#VALUE_NULL}.
     *
     * @param parser the parser to read from
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void expectNullValue(final JsonParser parser) throws IOException {

        if (parser.nextToken() != JsonToken.VALUE_NULL) { throw new JsonParseException("expected null value", parser.getCurrentLocation()); }
    }

    /**
     * Reads a field value as the provided type.
     *
     * @param <Value> the value type
     * @param parser the parser
     * @param expected_type the expected type of the value
     * @return the value
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static <Value> Value readValueAs(final JsonParser parser, final Class<Value> expected_type) throws IOException {

        final Value value;
        if (expected_type.equals(Void.TYPE)) {
            expectNullValue(parser);
            value = null;
        }
        else {
            parser.nextToken();
            value = parser.readValueAs(expected_type);
        }
        return value;
    }

    static RuntimeTypeReference toTypeReference(final Type type) {

        return new RuntimeTypeReference(type);
    }

    private static final class RuntimeTypeReference extends TypeReference<Object> {

        private final Type expected_type;

        private RuntimeTypeReference(final Type expected_type) {

            this.expected_type = expected_type;
        }

        @Override
        public Type getType() {

            return expected_type;
        }
    }
}
