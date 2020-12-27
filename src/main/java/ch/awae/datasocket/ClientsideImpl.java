package ch.awae.datasocket;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

class ClientsideImpl extends BaseSocket implements DataSocket {

    private final ConcurrentHashMap<UUID, CompletableFuture<Serializable>> futureMap;
    private final TypedMapping<Consumer<Serializable>> broadcastMappings;

    ClientsideImpl(Socket socket, ExecutorService service, TypedMapping<Consumer<Serializable>> broadcastMappings) throws IOException {
        super(socket, service);
        this.broadcastMappings = broadcastMappings;
        this.futureMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void handleMessage(Message message) {
        switch (message.getType()) {
            case BROADCAST:
                onBroadcast(message);
                break;
            case RESPONSE:
                onResponse(message);
                break;
            default:
                // unsupported messages get ignored silently
                break;
        }
    }

    private void onBroadcast(Message message) {
        Serializable payload = message.getPayload();
        if (payload != null) {
            broadcastMappings.get(payload.getClass()).accept(payload);
        }
    }

    private void onResponse(Message message) {
        CompletableFuture<Serializable> future = futureMap.remove(message.getUuid());
        Serializable payload = message.getPayload();
        if (payload instanceof RuntimeException) {
            future.completeExceptionally((RuntimeException) payload);
        } else {
            future.complete(payload);
        }
    }

    @Override
    protected void onConnectionClosed() {
        // no action needed here
    }

    @Override
    public void close() throws IOException {
        closeConnection();
    }

    @Override
    public <T extends Serializable> Future<T> query(Serializable object) throws IOException {
        UUID uuid = UUID.randomUUID();
        CompletableFuture<T> future = new CompletableFuture<>();
        futureMap.put(uuid, (CompletableFuture<Serializable>) future);
        send(new Message(MessageType.QUERY, uuid, object));
        return future;
    }
}
