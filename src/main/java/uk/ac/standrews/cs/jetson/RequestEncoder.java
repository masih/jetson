package uk.ac.standrews.cs.jetson;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundMessageHandlerAdapter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import uk.ac.standrews.cs.jetson.exception.InternalException;
import uk.ac.standrews.cs.jetson.exception.TransportException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Sharable
class RequestEncoder extends ChannelOutboundMessageHandlerAdapter<Request> {

    private static final Logger LOGGER = Logger.getLogger(RequestEncoder.class.getName());
    private final JsonFactory json_factory;

    RequestEncoder(final JsonFactory json_factory) {

        this.json_factory = json_factory;

    }

    @Override
    public void flush(final ChannelHandlerContext ctx, final Request request) throws Exception {

        final ByteBufOutputStream out = new ByteBufOutputStream(ctx.nextOutboundByteBuffer());
        JsonGenerator generator = null;
        try {
            generator = json_factory.createGenerator(out, JsonEncoding.UTF8);
            final Method target_method = request.getMethod();
            final Class<?>[] param_types = target_method.getParameterTypes();

            final ObjectMapper mapper = (ObjectMapper) generator.getCodec();
            generator.writeStartObject();
            generator.writeObjectField(Message.VERSION_KEY, request.getVersion());
            generator.writeObjectField(Request.METHOD_NAME_KEY, request.getMethodName());
            generator.writeArrayFieldStart(Request.PARAMETERS_KEY);
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
            generator.writeObjectField(Message.ID_KEY, request.getId());
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
