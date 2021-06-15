package ru.hse.servers.async;

import ru.hse.servers.async.callbacks.ReadMessageCompletionHandler;
import ru.hse.servers.async.callbacks.ReadSizeCompletionHandler;
import ru.hse.servers.async.callbacks.WriteCompletionHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;

public class Client {
    private final AsynchronousSocketChannel socketChannel;
    private final ExecutorService workerThreadPool;

    private ByteBuffer requests = null;

    private volatile boolean isWorkingFlag = true;

    public Client(AsynchronousSocketChannel socketChannel, ExecutorService workerThreadPool) {
        this.socketChannel = socketChannel;
        this.workerThreadPool = workerThreadPool;
    }

    public void run() {
        readSize();
    }

    public void stop() {
        try {
            isWorkingFlag = false;
            socketChannel.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void readSize() {
        requests = ByteBuffer.allocate(4);
        socketChannel.read(requests, this, new ReadSizeCompletionHandler());
    }

    public void readMsg(int msgSize) {
        requests = ByteBuffer.allocate(msgSize);
        socketChannel.read(requests, this, new ReadMessageCompletionHandler());
    }

    public void writeData(byte[] data) {
        ByteBuffer response = ByteBuffer.allocate(data.length + 4);
        response.putInt(data.length);
        response.put(data);
        response.flip();
        socketChannel.write(response, new WriteWrapper(this, response), new WriteCompletionHandler());
    }

    public ByteBuffer getRequests() {
        return requests;
    }

    public boolean isWorking() {
        return isWorkingFlag;
    }

    public AsynchronousSocketChannel getSocketChannel() {
        return socketChannel;
    }

    public ExecutorService getWorkerThreadPool() {
        return workerThreadPool;
    }
}