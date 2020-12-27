package ch.awae.datasocket;

import java.io.IOException;
import java.io.Serializable;

public interface DataSocketServer {

    void close() throws IOException;

    void send(Serializable message);

}
