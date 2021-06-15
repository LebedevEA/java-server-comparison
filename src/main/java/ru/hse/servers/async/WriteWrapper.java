package ru.hse.servers.async;


import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class WriteWrapper {
    private final AsyncClientHandler client;
    private final ByteBuffer buffer;

    public WriteWrapper(AsyncClientHandler client, ByteBuffer response) {
        this.client = client;
        this.buffer = response;
    }

    public boolean isWorking() {
        return client.isWorking();
    }

    public AsynchronousSocketChannel getSocketChannel() {
        return client.getSocketChannel();
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void restart() {
        client.run();
    }
}

