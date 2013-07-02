package com.staticiser.jetson.lean.codec;

import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class CollectionCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && isCollection(type) || isParameterizedCollection(type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value == null) {
            out.writeInt(-1);
        }
        else {

            final Collection collection = (Collection) value;
            final Type component_type = getComponentType(type);
            final int size = collection.size();
            out.writeInt(size);
            for (Object element : collection) {
                codecs.encodeAs(element, out, component_type);
            }
        }
    }

    @Override
    public Collection decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final int length = in.readInt();
        if (length < 0) { return null; }
        final Collection result = constructCollectionOfType(type);
        final Type component_type = getComponentType(type);
        for (int i = 0; i < length; i++) {
            result.add(codecs.decodeAs(in, component_type));
        }
        return result;

    }

    protected boolean isParameterizedCollection(final Type type) {

        return type != null && type instanceof ParameterizedType && Collection.class.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType());
    }

    protected boolean isCollection(final Type type) {

        return type instanceof Class<?> && Collection.class.isAssignableFrom((Class<?>) type);

    }

    protected Type getComponentType(final Type type) {

        return type instanceof ParameterizedType ? ((ParameterizedType) type).getActualTypeArguments()[0] : Object.class;
    }

    protected abstract Collection constructCollectionOfType(final Type type);
}
