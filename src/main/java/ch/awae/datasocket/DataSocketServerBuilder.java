package ch.awae.datasocket;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public final class DataSocketServerBuilder {

    private int port;
    private ExecutorService service;

    private final HashMap<Class<?>, Function<Serializable, Serializable>> handlers = new HashMap<>();

    public DataSocketServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    public DataSocketServerBuilder executor(ExecutorService service) {
        this.service = service;
        return this;
    }

    public <T extends Serializable> DataSocketServerBuilder handler(Class<T> tClass, Function<? super T, ? extends Serializable> handler) {
        this.handlers.put(tClass, (Function<Serializable, Serializable>) handler);
        return this;
    }

    public DataSocketServer build() throws IOException {
        Objects.requireNonNull(service);

        TypedMapping<Function<Serializable, Serializable>> mapping = new TypedMapping<>(this.handlers);
        ServerSocket serverSocket = new ServerSocket(port);
        ServerImpl server = new ServerImpl(serverSocket, service, mapping);
        server.start();
        return server;
    }

}
