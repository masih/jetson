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
