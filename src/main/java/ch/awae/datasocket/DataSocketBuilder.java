package ch.awae.datasocket;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public final class DataSocketBuilder {

    private String host;
    private int port;
    private ExecutorService service;

    private final HashMap<Class<?>, Consumer<Serializable>> handlers = new HashMap<>();

    public DataSocketBuilder address(String host, int port) {
        this.host = host;
        this.port = port;
        return this;
    }

    public DataSocketBuilder executor(ExecutorService service) {
        this.service = service;
        return this;
    }

    public <T extends Serializable> DataSocketBuilder handler(Class<T> tClass, Consumer<? super T> handler) {
        this.handlers.put(tClass, (Consumer<Serializable>) handler);
        return this;
    }

    public DataSocket build() throws IOException {
        Objects.requireNonNull(host);
        Objects.requireNonNull(service);

        TypedMapping<Consumer<Serializable>> mapping = new TypedMapping<>(this.handlers);
        Socket socket = new Socket(host, port);
        ClientsideImpl connection = new ClientsideImpl(socket, service, mapping);
        connection.start();
        return connection;
    }

}
