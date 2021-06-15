package ru.hse.servers.nonblocking;

import ru.hse.utils.ArrayHolder;
import ru.hse.utils.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static ru.hse.utils.Utils.bubbleSort;

public class NonBlockingClientHandler {
    private final SocketChannel socketChannel;
    private final ExecutorService workerThreadPool;
    private final ByteBuffer requests = ByteBuffer.allocate(1024 * 16);
    private final Queue<ByteBuffer> responses = new ConcurrentLinkedQueue<>();
    private int msgSize = -1;

    private final Consumer<NonBlockingClientHandler> registerResponse;

    private volatile boolean isWorking = true;
    private final AtomicInteger isDone = new AtomicInteger(0);

    public NonBlockingClientHandler(SocketChannel socketChannel, ExecutorService workerThreadPool, Consumer<NonBlockingClientHandler> registerResponse) {
        this.socketChannel = socketChannel;
        this.workerThreadPool = workerThreadPool;
        this.registerResponse = registerResponse;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }
    public void write() throws IOException {
        if (responses.isEmpty()) return;
        socketChannel.write(responses.peek());
        if (!responses.isEmpty() && !responses.peek().hasRemaining()) {
            responses.poll();
        }
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
        isDone.incrementAndGet();
        ArrayHolder data = Utils.readArray(buf);
        workerThreadPool.submit(() -> {
            bubbleSort(data.getArray());
            sendResponse(data.getArray(), data.getId());
        });
    }

    private void sendResponse(int[] data, int id) {
        if (!isWorking) return;

        byte[] toSend = Utils.serializeArray(data, id);
        ByteBuffer response = ByteBuffer.allocate(toSend.length + 4);
        response.putInt(toSend.length);
        response.put(toSend);
        response.flip();
        responses.offer(response);
        registerResponse.accept(this);
    }

    public boolean wantsWrite() {
        return !responses.isEmpty();
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
        return isDone.get() == 2;
    }
}
