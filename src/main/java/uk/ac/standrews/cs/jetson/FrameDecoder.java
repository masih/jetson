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
package uk.ac.standrews.cs.jetson;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;

/**
 * Splits the received bytes by {@link Delimiters#lineDelimiter() new_line} as the delimiter.
 * 
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 * @see DelimiterBasedFrameDecoder
 */
public class FrameDecoder extends DelimiterBasedFrameDecoder {

    /** The default maximum size of JSON RPC message frame. */
    public static final int DEFAULT_MAX_FRAME_LENGTH = 8192;

    static final String NAME = "framer";
    private static final ByteBuf[] FRAME_DELIMITER = Delimiters.nulDelimiter();
    static final String FRAME_DELIMITER_AS_STRING = new String(FRAME_DELIMITER[0].array());

    /** Instantiates a new frame decoder with the maximum frame size of {@value #DEFAULT_MAX_FRAME_LENGTH}. */
    public FrameDecoder() {

        this(DEFAULT_MAX_FRAME_LENGTH);
    }

    /**
     * Instantiates a new frame decoder.
     *
     * @param max_frame_length the maximum frame length
     */
    public FrameDecoder(final int max_frame_length) {

        super(max_frame_length, FRAME_DELIMITER);
    }

}
