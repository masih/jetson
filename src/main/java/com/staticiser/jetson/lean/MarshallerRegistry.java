package com.staticiser.jetson.lean;

import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class MarshallerRegistry {

    public static final StringMarshaller STRING_MARSHALLER = new StringMarshaller();
    public static final BooleanMarshaller BOOLEAN_MARSHALLER = new BooleanMarshaller();
    public static final ThrowableMarshaller THROWABLE_MARSHALLER = new ThrowableMarshaller();
    private static final int NULL = -1;
    private static final IntegerMarshaller INTEGER_MARSHALLER = new IntegerMarshaller();
    private static final ByteMarshaller BYTE_MARSHALLER = new ByteMarshaller();
    public static final CharacterMarshaller CHARACTER_MARSHALLER = new CharacterMarshaller();
    private final Map<Class, Marshaller> marshallers;

    public MarshallerRegistry() {
        marshallers = new HashMap<Class, Marshaller>();
        register(Byte.TYPE, BYTE_MARSHALLER);
        register(Byte.class, BYTE_MARSHALLER);
        register(Character.TYPE, CHARACTER_MARSHALLER);
        register(Character.class, CHARACTER_MARSHALLER);
        register(Boolean.TYPE, BOOLEAN_MARSHALLER);
        register(Boolean.class, BOOLEAN_MARSHALLER);
        register(Integer.TYPE, INTEGER_MARSHALLER);
        register(Integer.class, INTEGER_MARSHALLER);
        register(String.class, STRING_MARSHALLER);
        register(Throwable.class, THROWABLE_MARSHALLER);
        register(Exception.class, THROWABLE_MARSHALLER);

    }

    public <T> Marshaller<T> register(Class<? extends T> type, Marshaller<T> marshaller) {

        return marshallers.put(type, marshaller);
    }

    public Marshaller get(Type type) {

        final Marshaller marshaller = marshallers.get(type);
        if (marshaller == null && type instanceof Class) {
            Class c = (Class) type;
            if (Throwable.class.isAssignableFrom(c)) {
                return marshallers.get(Throwable.class);
            }
        }
        return marshaller;
    }

    //FIXME fix null values

    private static class IntegerMarshaller implements Marshaller<Integer> {

        @Override
        public void write(final Integer value, final ByteBuf out) throws RPCException {
            out.writeInt(value);
        }

        @Override
        public Integer read(final ByteBuf in) throws RPCException {
            return in.readInt();
        }
    }

    private static class BooleanMarshaller implements Marshaller<Boolean> {

        @Override
        public void write(final Boolean value, final ByteBuf out) throws RPCException {

            out.writeBoolean(value);
        }

        @Override
        public Boolean read(final ByteBuf in) throws RPCException {
            return in.readBoolean();
        }
    }

    private static class CharacterMarshaller implements Marshaller<Character> {

        @Override
        public void write(final Character value, final ByteBuf out) throws RPCException {

            out.writeChar(value);
        }

        @Override
        public Character read(final ByteBuf in) throws RPCException {
            return in.readChar();
        }
    }

    private static class ByteMarshaller implements Marshaller<Byte> {

        @Override
        public void write(final Byte value, final ByteBuf out) throws RPCException {

            out.writeByte(value);
        }

        @Override
        public Byte read(final ByteBuf in) throws RPCException {
            return in.readByte();
        }
    }

    private static class StringMarshaller implements Marshaller<String> {

        public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

        @Override
        public void write(final String value, final ByteBuf out) throws RPCException {

            if (value == null) {
                out.writeInt(0);
            }
            else {
                final byte[] bytes = value.getBytes(DEFAULT_CHARSET);
                out.writeInt(bytes.length);
                out.writeBytes(bytes);
            }
        }

        @Override
        public String read(final ByteBuf in) throws RPCException {

            final int size = in.readInt();
            if (size == 0) { return null; }
            final byte[] bytes = new byte[size];
            in.readBytes(bytes);
            return new String(bytes, DEFAULT_CHARSET);
        }
    }

    private static class ThrowableMarshaller implements Marshaller<Throwable> {

        @Override
        public void write(final Throwable throwable, final ByteBuf out) throws RPCException {
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(new ByteBufOutputStream(out));
                throwable.setStackTrace(new StackTraceElement[0]);  //Skip stack trace
                oos.writeObject(throwable);
                oos.flush();
            }
            catch (IOException e) {
                throw new RPCException(e);
            }
            finally {
                if (oos != null) {
                    try {
                        oos.close();
                    }
                    catch (IOException e) {
                        //ignore
                    }
                }
            }
        }

        @Override
        public Throwable read(final ByteBuf in) throws RPCException {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new ByteBufInputStream(in));
                return (Throwable) ois.readObject();
            }
            catch (IOException e) {
                throw new RPCException(e);
            }
            catch (ClassNotFoundException e) {
                throw new RPCException(e);
            }
            finally {
                if (ois != null) {
                    try {
                        ois.close();
                    }
                    catch (IOException e) {
                        //ignore
                    }
                }
            }
        }
    }

}
