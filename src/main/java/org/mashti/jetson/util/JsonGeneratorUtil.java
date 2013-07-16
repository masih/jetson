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
package org.mashti.jetson.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * A utility class for generating JSON using {@link JsonGenerator}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class JsonGeneratorUtil {

    private JsonGeneratorUtil() {

    }

    /**
     * Writes the given {@code values} as a JSON array with the given field name and serialises each value as a provided type.
     * An {@link ObjectMapper} must be present as the {@code generator}'s {@link JsonGenerator#getCodec() codec}.
     * For each value in the given {@code values}, a type must be provided under the same index in {@code value_types}.
     * This method supports parameterised types.
     *
     * @param generator the JSON generator
     * @param field_name the field name of the JSON array
     * @param value_types the types of the values
     * @param values the values
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void writeValuesAs(final JsonGenerator generator, final String field_name, final Type[] value_types, final Object[] values) throws IOException {

        generator.writeArrayFieldStart(field_name);
        if (values != null && values.length > 0) {
            final ObjectMapper mapper = (ObjectMapper) generator.getCodec();
            int i = 0;
            for (final Object value : values) {
                final Type value_type = value_types[i++];
                final ObjectWriter writer = mapper.writerWithType(JsonParserUtil.toTypeReference(value_type));
                writer.writeValue(generator, value);
            }
        }
        generator.writeEndArray();
    }
}
