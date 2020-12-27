package ch.awae.datasocket;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

class ServerImpl extends Thread implements DataSocketServer {

    private final ServerSocket serverSocket;
    private final ExecutorService service;
    private final TypedMapping<Function<Serializable, Serializable>> processorMappings;
    private final List<ServersideImpl> connections;

    ServerImpl(ServerSocket serverSocket, ExecutorService service, TypedMapping<Function<Serializable, Serializable>> processorMappings) {
        this.serverSocket = serverSocket;
        this.service = service;
        this.processorMappings = processorMappings;
        connections = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Socket socket = serverSocket.accept();
                ServersideImpl connection = new ServersideImpl(this, socket, service, processorMappings);
                connections.add(connection);
                connection.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        interrupt();
        serverSocket.close();
    }

    @Override
    public void send(Serializable payload) {
        Message message = new Message(MessageType.BROADCAST, null, payload);
        connections.forEach(c -> {
            try {
                c.send(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    void connectionClosed(ServersideImpl connection) {
        connections.remove(connection);
    }
}
