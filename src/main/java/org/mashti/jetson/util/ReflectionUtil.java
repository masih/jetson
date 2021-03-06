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

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.mashti.jetson.exception.RPCException;

/**
 * A utility class for reflective analysis of JSON RPC interfaces.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class ReflectionUtil {

    private static final MethodComparator METHOD_COMPARATOR = new MethodComparator();
    private static final Map<Class<?>, Map<String, Method>> CACHED_DISPATCH_MAPS = new HashMap<Class<?>, Map<String, Method>>();

    private ReflectionUtil() {

    }

    public static Class<?> getRawClass(final Type type) {

        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterized_type = (ParameterizedType) type;
            return getRawClass(parameterized_type.getRawType());
        }
        if (type instanceof GenericArrayType) {
            final GenericArrayType generic_array_type = (GenericArrayType) type;
            return getRawClass(generic_array_type.getGenericComponentType());
        }

        return null;
    }

    /**
     * Checks if the given {@link Type type} is a primitive type.
     *
     * @param type the type to check
     * @return {@code true} if the given {@code type} presents a primitive type; {@code false otherwise}
     * @see Class#isPrimitive()
     */
    public static boolean isPrimitive(final Type type) {

        return type != null && type instanceof Class<?> && ((Class<?>) type).isPrimitive();
    }

    /**
     * Maps .
     *
     * @param service the service
     * @return the map
     */
    public static Map<Method, String> mapMethodsToNames(final Class<?> service) {

        final Map<String, Method> dispatch = mapNamesToMethods(service);
        return inverse(dispatch);
    }

    /**
     * Maps names to {@link Class#getMethods() methods} of the given type.
     * If the given type contains overloaded methods, a unique name is mapped to its name. The chosen name is typically the original name with an integer.
     * The mapped methods are cached and will be reused.
     *
     * @param service the service
     * @return the map
     * @throws IllegalArgumentException if one of the methods in the given type do not throw {@link RPCException} or one of its super types
     */
    public static Map<String, Method> mapNamesToMethods(final Class<?> service) {

        if (isCached(service)) { return getCachedDispatchMap(service); }
        final Method[] methods = checkAndSort(service.getMethods());

        final Map<String, Method> dispatch_map = new HashMap<String, Method>();
        int i = 1;
        for (final Method method : methods) {
//            validateJsonRpcExceptionTypes(method);

            String method_name = method.getName();
            if (dispatch_map.containsKey(method_name)) { // check if method is overloaded
                method_name += i++;
            }
            dispatch_map.put(method_name, method);
        }

        cache(service, dispatch_map);
        return dispatch_map;

    }

    public static Method[] checkAndSort(final Method... methods) {

        for (Method method : methods) {
            if (!CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
                throw new RuntimeException("method return types must be of type " + CompletableFuture.class);
            }
        }

        Arrays.sort(methods, METHOD_COMPARATOR);
        return methods;
    }

    private static Map<Method, String> inverse(final Map<String, Method> dispatch) {

        final Map<Method, String> reverse_dispatch = new HashMap<Method, String>();
        for (final Entry<String, Method> entry : dispatch.entrySet()) {
            reverse_dispatch.put(entry.getValue(), entry.getKey());
        }
        return reverse_dispatch;
    }

    private static Map<String, Method> getCachedDispatchMap(final Class<?> service) {

        return CACHED_DISPATCH_MAPS.get(service);
    }

    private static boolean isCached(final Class<?> service) {

        return CACHED_DISPATCH_MAPS.containsKey(service);
    }

    private static void cache(final Class<?> service, final Map<String, Method> dispatch_map) {

        if (!isCached(service)) {
            CACHED_DISPATCH_MAPS.put(service, dispatch_map);
        }
    }

    private static void validateJsonRpcExceptionTypes(final Method method) {

        if (!containsAnyAssignableFrom(RPCException.class, method.getExceptionTypes())) {
            throw new IllegalArgumentException(method + " must throw RPCException or one of its super types");
        }
    }

    /**
     * Checks if the given {@code assignee} is {@link Class#isAssignableFrom(Class) assignable} from at least one of the given types.
     *
     * @param assignee the type to assign
     * @param types the types to check its assignability
     * @return true, if the given {@code assignee} is assignable from at least one of the given types
     */
    private static boolean containsAnyAssignableFrom(final Class<?> assignee, final Class<?>[] types) {

        for (final Class<?> exception_type : types) {
            if (exception_type.isAssignableFrom(assignee)) { return true; }
        }
        return false;
    }

    private static final class MethodComparator implements Comparator<Method>, Serializable {

        private static final long serialVersionUID = -4937883029462194071L;

        @Override
        public int compare(final Method first, final Method second) {

            return !first.equals(second) ? compareToGenericString(first, second) : 0;
        }

        private int compareToGenericString(final Method first, final Method second) {

            final String first_to_generic_string = first.toGenericString();
            final String second_to_generic_string = second.toGenericString();
            return first_to_generic_string.compareTo(second_to_generic_string);
        }
    }
}
