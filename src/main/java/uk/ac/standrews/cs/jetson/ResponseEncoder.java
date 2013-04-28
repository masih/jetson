package uk.ac.standrews.cs.jetson;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundMessageHandlerAdapter;

import java.io.IOException;

import uk.ac.standrews.cs.jetson.exception.InternalException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

@Sharable
class ResponseEncoder extends ChannelOutboundMessageHandlerAdapter<Response> {

    private final JsonFactory json_factory;

    ResponseEncoder(final JsonFactory json_factory) {

        this.json_factory = json_factory;
    }

    @Override
    public void flush(final ChannelHandlerContext ctx, final Response response) throws Exception {

        JsonGenerator generator = null;
        try {
            generator = json_factory.createGenerator(new ByteBufOutputStream(ctx.nextOutboundByteBuffer()));
            generator.writeStartObject();
            generator.writeObjectField(Message.VERSION_KEY, response.getVersion());
            if (!response.isError()) {
                generator.writeObjectField(Response.RESULT_KEY, response.getResult());
            }
            else {
                generator.writeObjectField(Response.ERROR_KEY, response.getError());
            }
            generator.writeObjectField(Message.ID_KEY, response.getId());
            generator.writeEndObject();
            generator.writeRaw('\n');
            generator.flush();
            generator.close();
        }
        catch (final IOException e) {
            throw new InternalException(e);
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
