package com.staticiser.jetson.lean;

import com.staticiser.jetson.exception.RPCException;
import io.netty.buffer.ByteBuf;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface Marshaller<Value> {

    void write(Value value, ByteBuf out) throws RPCException;

    Value read(ByteBuf in) throws RPCException;

}
