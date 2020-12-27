package ch.awae.datasocket;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

class ServersideImpl extends BaseSocket {

    private final ServerImpl server;
    private final TypedMapping<Function<Serializable, Serializable>> processorMappings;

    ServersideImpl(ServerImpl server, Socket socket, ExecutorService service, TypedMapping<Function<Serializable, Serializable>> processorMappings) throws IOException {
        super(socket, service);
        this.server = server;
        this.processorMappings = processorMappings;
    }

    @Override
    protected void handleMessage(Message message) throws IOException {
        if (message.getType() == MessageType.QUERY) {
            try {
                Function<Serializable, Serializable> function = processorMappings.get(message.getPayload().getClass());
                Serializable response = function.apply(message.getPayload());
                send(new Message(MessageType.RESPONSE, message.getUuid(), response));
            } catch (RuntimeException rex) {
                send(new Message(MessageType.RESPONSE, message.getUuid(), rex));
            }
        }
    }

    @Override
    protected void onConnectionClosed() {
        server.connectionClosed(this);
    }
}
