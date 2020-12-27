package ch.awae.datasocket;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

abstract class BaseSocket extends Thread {

    private final Socket socket;
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;
    private final ExecutorService service;

    private final Object SEND_LOCK = new Object();
    private int messageCounter = 0;

    BaseSocket(Socket socket, ExecutorService service) throws IOException {
        this.socket = socket;

        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
        this.service = service;
    }

    void send(Message message) throws IOException {
        synchronized (SEND_LOCK) {
            oos.writeObject(message);
            messageCounter++;
            if (messageCounter >= 100) {
                oos.reset();
            }
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                Message message = (Message) ois.readObject();
                service.submit(() -> {
                    try {
                        handleMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (ClassNotFoundException | InvalidClassException | RuntimeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                onConnectionClosed();
                return;
            }
        }
    }

    void closeConnection() throws IOException {
        socket.close();
    }

    protected abstract void handleMessage(Message message) throws IOException;

    protected abstract void onConnectionClosed();
}
