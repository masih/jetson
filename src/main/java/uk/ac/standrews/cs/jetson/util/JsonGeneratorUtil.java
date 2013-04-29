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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonGeneratorUtil {

    private JsonGeneratorUtil() {

    }

    public static void writeValuesAs(final JsonGenerator generator, final String field_name, final Type[] value_types, final Object[] values) throws JsonGenerationException, IOException {

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
