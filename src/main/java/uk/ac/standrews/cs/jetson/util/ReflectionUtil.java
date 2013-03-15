/*
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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.standrews.cs.jetson.exception.JsonRpcException;

public class ReflectionUtil {

    private static final MethodComparator METHOD_COMPARATOR = new MethodComparator();
    private static final Map<Class<?>, Map<String, Method>> CACHED_DISPATCH_MAPS = new HashMap<Class<?>, Map<String, Method>>();

    public static Map<Method, String> mapMethodsToNames(final Class<?> service) {

        final Map<String, Method> dispatch = mapNamesToMethods(service);
        return inverse(dispatch);
    }

    private static Map<Method, String> inverse(final Map<String, Method> dispatch) {

        final Map<Method, String> reverse_dispatch = new HashMap<Method, String>();
        for (final Entry<String, Method> entry : dispatch.entrySet()) {
            reverse_dispatch.put(entry.getValue(), entry.getKey());
        }
        return reverse_dispatch;
    }

    public static Map<String, Method> mapNamesToMethods(final Class<?> service) {

        if (isCached(service)) { return getCachedDispatchMap(service); }
        final Method[] methods = sort(service.getMethods());

        final Map<String, Method> dispatch_map = new HashMap<String, Method>();
        int i = 1;
        for (final Method method : methods) {
            validateExceptionTypes(method.getExceptionTypes());

            String method_name = method.getName();
            if (dispatch_map.containsKey(method_name)) { // check if method is overloaded
                method_name += i++;
            }
            dispatch_map.put(method_name, method);
        }

        cache(service, dispatch_map);
        return dispatch_map;

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

    private static Method[] sort(final Method... methods) {

        Arrays.sort(methods, METHOD_COMPARATOR);
        return methods;
    }

    private static void validateExceptionTypes(final Class<?>... exception_types) {

        if (!containsAnyAssignableFrom(JsonRpcException.class, exception_types)) { throw new RuntimeException("must throw JsonRpcException"); }
    }

    public static boolean containsAnyAssignableFrom(final Class<?> assignable, final Class<?>[] exception_types) {

        for (final Class<?> exception_type : exception_types) {
            if (exception_type.isAssignableFrom(assignable)) { return true; }
        }
        return false;
    }

    private static final class MethodComparator implements Comparator<Method> {

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
