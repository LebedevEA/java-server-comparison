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

    private volatile boolean isWorking = true;

    public Client(AsynchronousSocketChannel socketChannel, ExecutorService workerThreadPool) {
        this.socketChannel = socketChannel;
        this.workerThreadPool = workerThreadPool;
    }

    public void run() {
        readSize();
    }

    private void readSize() {
        ByteBuffer size = ByteBuffer.allocate(4);
        socketChannel.read(
                size,
                new AsyncServer.ClientBufferWrapper(this, size),
                new CompletionHandler<>() {
                    @Override
                    public void completed(Integer result, AsyncServer.ClientBufferWrapper attachment) {
                        if (!isWorking || result == -1) return;

                        if (attachment.getBuffer().hasRemaining()) {
                            socketChannel.read(attachment.getBuffer(), attachment, this);
                            return;
                        }
                        attachment.getBuffer().flip();
                        int msgSize = attachment.getBuffer().getInt();
                        attachment.getClient().readMsg(msgSize);
                    }

                    @Override
                    public void failed(Throwable exc, AsyncServer.ClientBufferWrapper attachment) {
                        throw new RuntimeException(exc);
                    }
                }
        );
    }

    private void readMsg(int msgSize) {
        ByteBuffer msg = ByteBuffer.allocate(msgSize);
        socketChannel.read(
                msg,
                new AsyncServer.ClientBufferWrapper(this, msg),
                new CompletionHandler<>() {
                    @Override
                    public void completed(Integer result, AsyncServer.ClientBufferWrapper attachment) {
                        if (!isWorking || result == -1) return;

                        if (attachment.getBuffer().hasRemaining()) {
                            socketChannel.read(attachment.getBuffer(), attachment, this);
                        }
                        try {
                            int[] data = Utils.readArray(attachment.getBuffer().array());
                            handleData(data);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                        attachment.getClient().run();
                    }

                    @Override
                    public void failed(Throwable exc, AsyncServer.ClientBufferWrapper attachment) {
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
        ByteBuffer msg = ByteBuffer.allocate(data.length + 4);
        msg.putInt(data.length);
        msg.put(data);
        msg.flip();
        socketChannel.write(
                msg,
                new AsyncServer.ClientBufferWrapper(this, msg),
                new CompletionHandler<>() {
                    @Override
                    public void completed(Integer result, AsyncServer.ClientBufferWrapper attachment) {
                        if (!isWorking) return;

                        if (attachment.getBuffer().hasRemaining()) {
                            socketChannel.write(attachment.getBuffer(), attachment, this);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, AsyncServer.ClientBufferWrapper attachment) {
                        throw new RuntimeException(exc);
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
