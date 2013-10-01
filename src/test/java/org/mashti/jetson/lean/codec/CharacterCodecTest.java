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

import org.junit.Assert;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class CharacterCodecTest extends CodecTest {

    public CharacterCodecTest() {

        super(new CharacterCodec());
    }

    @Test
    public void testIsSupported() throws Exception {

        Assert.assertTrue(codec.isSupported(Character.class));
        Assert.assertTrue(codec.isSupported(Character.TYPE));
        Assert.assertFalse(codec.isSupported(null));
        Assert.assertFalse(codec.isSupported(Object.class));
    }

    @Test
    public void testCodec() throws Exception {

        final char[] chars = {' ', '@', 'a', '!', '\u02DA', '\u02FA'};
        for (final char b : chars) {
            encode(b, Character.TYPE);
        }
        // Assume UTF-8 encoding: 2 bytes for each character
        Assert.assertEquals(chars.length * 2, buffer.readableBytes());
        for (final char b : chars) {
            Assert.assertTrue(b == decode(Character.TYPE));
        }
    }
}
