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
package uk.ac.standrews.cs.jetson.util;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;

public class JsonParserUtil {

    private JsonParserUtil() {

    }

    public static String expectFieldNames(final JsonParser parser, final String... field_names) throws JsonParseException, IOException {

        final String next_field_name = nextFieldName(parser);
        for (final String field_name : field_names) {
            if (next_field_name.equals(field_name)) { return next_field_name; }
        }
        throw new JsonParseException("expected one of field names " + Arrays.toString(field_names), parser.getCurrentLocation());
    }

    public static void expectFieldName(final JsonParser parser, final String field_name) throws JsonParseException, IOException {

        if (!nextFieldName(parser).equals(field_name)) { throw new JsonParseException("expected the field name " + field_name, parser.getCurrentLocation()); }
    }

    public static String nextFieldName(final JsonParser parser) throws JsonParseException, IOException {

        if (parser.nextToken() == JsonToken.FIELD_NAME) { return parser.getCurrentName(); }
        throw new JsonParseException("expected some field name", parser.getCurrentLocation());
    }

    public static <T> T readFieldValueAs(final JsonParser parser, final String expected_filed_name, final Class<T> expected_type) throws JsonParseException, IOException {

        expectFieldName(parser, expected_filed_name);
        return readValueAs(parser, expected_type);
    }

    public static Object[] readArrayValuesAs(final JsonParser parser, final Type[] expected_types) throws JsonParseException, IOException {

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

    public static void expectStartArray(final JsonParser parser) throws JsonParseException, IOException {

        if (parser.nextToken() != JsonToken.START_ARRAY) { throw new JsonParseException("expected start array", parser.getCurrentLocation()); }
    }

    public static void expectEndArray(final JsonParser parser) throws JsonParseException, IOException {

        if (parser.nextToken() != JsonToken.END_ARRAY) { throw new JsonParseException("expected end array", parser.getCurrentLocation()); }
    }

    public static Object readValueAs(final JsonParser parser, final Type expected_type) throws JsonParseException, IOException {

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

    static RuntimeTypeReference toTypeReference(final Type type) {

        return new RuntimeTypeReference(type);
    }

    public static void expectNullValue(final JsonParser parser) throws JsonParseException, IOException {

        if (parser.nextToken() != JsonToken.VALUE_NULL) { throw new JsonParseException("expected null value", parser.getCurrentLocation()); }
    }

    public static <Value> Value readValueAs(final JsonParser parser, final Class<Value> expected_type) throws JsonParseException, IOException {

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
