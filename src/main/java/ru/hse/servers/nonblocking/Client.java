package ru.hse.servers.nonblocking;

import ru.hse.utils.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static ru.hse.utils.Utils.bubbleSort;

public class Client {
    private final SocketChannel socketChannel;
    private final ExecutorService workerThreadPool;
    private final ByteBuffer requests = ByteBuffer.allocate(1024 * 16);
    private final ByteBuffer responses = ByteBuffer.allocate(1024 * 16);
    private int msgSize = -1;

    private final Consumer<Client> registerResponse;

    private volatile boolean isWorking = true;
    private boolean isDone = false;

    public Client(SocketChannel socketChannel, ExecutorService workerThreadPool, Consumer<Client> registerResponse) {
        this.socketChannel = socketChannel;
        this.workerThreadPool = workerThreadPool;
        this.registerResponse = registerResponse;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }
    public void write() throws IOException {
        responses.flip();
        socketChannel.write(responses);
        responses.compact();
    }

    public void read() {
        try {
            socketChannel.read(requests);
            check();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void check() throws IOException {
        if (!isWorking) return;

        requests.flip();

        if (msgSize == -1) {
            if (!tryReadSize()) {
                requests.compact();
                return;
            }
        } else {
            if (!tryReadMessage()) {
                requests.compact();
                return;
            }
        }

        requests.compact();
        check();
    }

    private boolean tryReadSize() {
        if (requests.remaining() < 4) return false;
        msgSize = requests.getInt();
        return true;
    }

    private boolean tryReadMessage() throws IOException {
        if (requests.remaining() < msgSize) return false;
        byte[] buf = new byte[msgSize];
        requests.get(buf);
        msgSize = -1;

        handleMessage(buf);
        return true;
    }

    private void handleMessage(byte[] buf) throws IOException {
        isDone = true;
        int[] data = Utils.readArray(buf);
        workerThreadPool.submit(() -> {
            bubbleSort(data);
            sendResponse(data);
        });
    }

    private void sendResponse(int[] data) {
        if (!isWorking) return;

        byte[] toSend = Utils.serializeArray(data);
        responses.putInt(toSend.length);
        responses.put(toSend);
        registerResponse.accept(this);
    }

    public boolean wantsWrite() {
        responses.flip();
        boolean hasRemaining = responses.hasRemaining();
        responses.compact();
        return hasRemaining;
    }

    public void stop() {
        try {
            isWorking = false;
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean done() {
        return isDone;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                socketChannel,
                workerThreadPool,
                requests,
                responses,
                msgSize,
                registerResponse,
                isWorking,
                isDone
        );
    }
}
