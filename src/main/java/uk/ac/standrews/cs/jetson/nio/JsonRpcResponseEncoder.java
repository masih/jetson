package uk.ac.standrews.cs.jetson.nio;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundMessageHandlerAdapter;

import java.io.IOException;
import java.util.logging.Logger;

import uk.ac.standrews.cs.jetson.JsonRpcMessage;
import uk.ac.standrews.cs.jetson.JsonRpcResponse;
import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseError;
import uk.ac.standrews.cs.jetson.JsonRpcResponse.JsonRpcResponseResult;
import uk.ac.standrews.cs.jetson.exception.InternalException;
import uk.ac.standrews.cs.jetson.exception.JsonRpcError;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

@Sharable
public class JsonRpcResponseEncoder extends ChannelOutboundMessageHandlerAdapter<JsonRpcResponse> {

    private static final Logger LOGGER = Logger.getLogger(JsonRpcResponseEncoder.class.getName());
    private final JsonFactory json_factory;

    public JsonRpcResponseEncoder(final JsonFactory json_factory) {

        this.json_factory = json_factory;
    }

    @Override
    public void flush(final ChannelHandlerContext ctx, final JsonRpcResponse response) throws Exception {

        JsonGenerator generator = null;
        try {
            generator = json_factory.createGenerator(new ByteBufOutputStream(ctx.nextOutboundByteBuffer()));
            generator.writeStartObject();
            generator.writeObjectField(JsonRpcMessage.VERSION_KEY, response.getVersion());
            if (response instanceof JsonRpcResponseResult) {
                generator.writeObjectField(JsonRpcResponse.RESULT_KEY, ((JsonRpcResponseResult) response).getResult());
            }
            else {
                final JsonRpcError error = ((JsonRpcResponseError) response).getError();
                LOGGER.fine("error occured on server " + error);
                generator.writeObjectField(JsonRpcResponse.ERROR_KEY, error);
            }
            generator.writeObjectField(JsonRpcMessage.ID_KEY, response.getId());
            generator.writeEndObject();
            generator.writeRaw('\n');
            generator.flush();
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
