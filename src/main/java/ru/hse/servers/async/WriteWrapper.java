package ru.hse.servers.async;


import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class WriteWrapper {
    private Client client;
    private ByteBuffer buffer;

    public WriteWrapper(Client client, ByteBuffer response) {
        this.client = client;
        this.buffer = response;
    }

    public boolean isWorking() {
        return false;
    }

    public AsynchronousSocketChannel getSocketChannel() {
        return client.getSocketChannel();
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}

