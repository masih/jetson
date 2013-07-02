package com.staticiser.jetson.lean.codec;

import java.lang.reflect.Type;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public final class CodecUtils {

    private CodecUtils() {

    }

    /**
     * Checks if the given {@link Type type} is a primitive type.
     *
     * @param type the type to check
     * @return {@code true} if the given {@code type} presents a primitive type; {@code false otherwise}
     * @see Class#isPrimitive()
     */
    public static boolean isPrimitive(Type type) {

        return type != null && type instanceof Class<?> && ((Class<?>) type).isPrimitive();
    }
}
