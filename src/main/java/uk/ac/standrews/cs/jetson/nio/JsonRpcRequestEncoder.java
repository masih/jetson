package uk.ac.standrews.cs.jetson.nio;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundMessageHandlerAdapter;
import io.netty.util.AttributeKey;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import uk.ac.standrews.cs.jetson.JsonRpcMessage;
import uk.ac.standrews.cs.jetson.JsonRpcRequest;
import uk.ac.standrews.cs.jetson.exception.InternalException;
import uk.ac.standrews.cs.jetson.exception.TransportException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Sharable
public class JsonRpcRequestEncoder extends ChannelOutboundMessageHandlerAdapter<JsonRpcRequest> {

    private static final Logger LOGGER = Logger.getLogger(JsonRpcRequestEncoder.class.getName());
    private final JsonFactory json_factory;

    static final AttributeKey<CountDownLatch> RESPONSE_LATCH = new AttributeKey<CountDownLatch>("response_latch");

    public JsonRpcRequestEncoder(final JsonFactory json_factory) {

        this.json_factory = json_factory;

    }

    @Override
    public void flush(final ChannelHandlerContext ctx, final JsonRpcRequest request) throws Exception {

        ctx.channel().attr(JsonRpcResponseDecoder.REQUEST_ID_ATTRIBUTE).set(request.getId());
        ctx.channel().attr(JsonRpcResponseDecoder.RETURN_TYPE_ATTRIBUTE).set(request.getMethod().getGenericReturnType());
        ctx.channel().attr(RESPONSE_LATCH).set(new CountDownLatch(1));
        final ByteBufOutputStream out = new ByteBufOutputStream(ctx.nextOutboundByteBuffer());
        JsonGenerator generator = null;
        try {
            generator = json_factory.createGenerator(out, JsonEncoding.UTF8);
            final Method target_method = request.getMethod();
            final Class<?>[] param_types = target_method.getParameterTypes();
            //                LOGGER.fine(((ObjectMapper) generator.getCodec()).writeValueAsString(request));

            final ObjectMapper mapper = (ObjectMapper) generator.getCodec();
            generator.writeStartObject();
            generator.writeObjectField(JsonRpcMessage.VERSION_KEY, request.getVersion());
            generator.writeObjectField(JsonRpcRequest.METHOD_NAME_KEY, request.getMethodName());
            generator.writeArrayFieldStart(JsonRpcRequest.PARAMETERS_KEY);
            if (request.getParameters() != null) {
                int i = 0;
                for (final Object param : request.getParameters()) {

                    final Class<?> static_param_type = param_types[i++];
                    if (!mapper.canSerialize(static_param_type)) {
                        LOGGER.warning("No serializer is found for the type" + static_param_type + " at " + target_method);
                    }

                    final ObjectWriter writer = mapper.writerWithType(static_param_type);
                    //FIXME cache writers
                    writer.writeValue(generator, param);
                }
            }
            generator.writeEndArray();
            generator.writeObjectField(JsonRpcMessage.ID_KEY, request.getId());
            generator.writeEndObject();
            generator.writeRaw('\n');
            generator.flush();
            generator.close();
        }
        catch (final IOException e) {
            throw new TransportException(e);
        }
        finally {
            if (generator != null) {
                try {
                    generator.close();
                }
                catch (final IOException e) {
                    throw new InternalException(e);
                }
            }
        }
    }
}
