package ru.hse.servers.async;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.hse.utils.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;

import static ru.hse.utils.Utils.bubbleSort;
import static ru.hse.utils.Utils.serializeArray;

public class Client {
    private final AsynchronousSocketChannel socketChannel;
    private final ExecutorService workerThreadPool;

    private ByteBuffer requests = null;

    private volatile boolean isWorking = true;

    public Client(AsynchronousSocketChannel socketChannel, ExecutorService workerThreadPool) {
        this.socketChannel = socketChannel;
        this.workerThreadPool = workerThreadPool;
    }

    public void run() {
        readSize();
    }

    private void readSize() {
        requests = ByteBuffer.allocate(4);
        socketChannel.read(
                requests,
                this,
                new CompletionHandler<>() {
                    @Override
                    public void completed(Integer result, Client attachment) {
                        if (!isWorking || result == -1) return;

                        if (attachment.requests.hasRemaining()) {
                            socketChannel.read(attachment.requests, attachment, this);
                            return;
                        }
                        attachment.requests.flip();
                        int msgSize = attachment.requests.getInt();
                        attachment.readMsg(msgSize);
                    }

                    @Override
                    public void failed(Throwable exc, Client attachment) {
                        throw new RuntimeException(exc);
                    }
                }
        );
    }

    private void readMsg(int msgSize) {
        requests = ByteBuffer.allocate(msgSize);
        socketChannel.read(
                requests,
                this,
                new CompletionHandler<>() {
                    @Override
                    public void completed(Integer result, Client attachment) {
                        if (!isWorking || result == -1) return;

                        if (attachment.requests.hasRemaining()) {
                            socketChannel.read(attachment.requests, attachment, this);
                        }
                        try {
                            int[] data = Utils.readArray(attachment.requests.array());
                            handleData(data);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                        attachment.run();
                    }

                    @Override
                    public void failed(Throwable exc, Client attachment) {
                        throw new RuntimeException(exc);
                    }
                }
        );
    }

    private void handleData(int[] data) {
        workerThreadPool.submit(() -> {
            bubbleSort(data);
            writeData(serializeArray(data));
        });
    }

    private void writeData(byte[] data) {
        ByteBuffer response = ByteBuffer.allocate(data.length + 4);
        response.putInt(data.length);
        response.put(data);
        response.flip();
        socketChannel.write(
                response,
                new WriteWrapper(socketChannel, response),
                new CompletionHandler<>() {
                    @Override
                    public void completed(Integer result, WriteWrapper attachment) {
                        if (attachment.buffer.hasRemaining()) {
                            attachment.socketChannel.write(attachment.buffer, attachment, this);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, WriteWrapper attachment) {

                    }
                }
        );
    }

    public void stop() {
        try {
            isWorking = false;
            socketChannel.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}

class WriteWrapper {
    AsynchronousSocketChannel socketChannel;
    ByteBuffer buffer;

    public WriteWrapper(AsynchronousSocketChannel socketChannel, ByteBuffer response) {
        this.socketChannel = socketChannel;
        this.buffer = response;
    }
}
