package ch.awae.datasocket;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Future;

public interface DataSocket {

    void close() throws IOException;

    <T extends Serializable> Future<T> query(Serializable object) throws IOException;

}
