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
package org.mashti.jetson.lean.codec;

import io.netty.buffer.ByteBuf;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class IntegerCodec extends PrimitiveTypeCodec {

    IntegerCodec() {

        super(Integer.class, Integer.TYPE);
    }

    @Override
    protected Object readValue(final ByteBuf in) {

        return in.readInt();
    }

    @Override
    protected void writeValue(final ByteBuf out, final Object value) {

        out.writeInt((Integer) value);
    }
}
