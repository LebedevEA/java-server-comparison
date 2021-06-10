package ru.hse.servers;

import ru.hse.utils.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.hse.utils.Constants.PORT;
import static ru.hse.utils.Utils.bubbleSort;

public class NonBlockingServer implements Server {
    private ServerSocketChannel serverSocketChannel = null;
    private Selector requestSelector = null;
    private Selector responseSelector = null;

    private final ExecutorService serverSocketService = Executors.newSingleThreadExecutor();
    private final ExecutorService requestSelectorService = Executors.newSingleThreadExecutor();
    private final ExecutorService responseSelectorService = Executors.newSingleThreadExecutor();
    private final ExecutorService workerThreadPool = Executors.newFixedThreadPool(16);

    private final Queue<SocketChannel> toRegisterRequests = new ConcurrentLinkedQueue<>();
    private final Queue<Client> toRegisterResponses = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap.KeySetView<Client, Boolean> toUnregister = ConcurrentHashMap.newKeySet();

    private volatile boolean isWorking = true;
    private final Set<Client> clients = new HashSet<>();

    @Override
    public void start() throws IOException {
        serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(PORT));
        serverSocketService.submit(this::acceptClients);

        requestSelector = Selector.open();
        requestSelectorService.submit(this::receive);

        responseSelector = Selector.open();
        responseSelectorService.submit(this::send);
    }

    @Override
    public void stop() throws IOException {
        isWorking = false;
        serverSocketChannel.close();

        serverSocketService.shutdown();
        workerThreadPool.shutdown();
        requestSelectorService.shutdown();
        responseSelectorService.shutdown();

        responseSelector.wakeup();
        requestSelector.wakeup();

        clients.forEach(Client::close);
    }

    private void send() {
        try {
            while (!Thread.interrupted() && isWorking) {
                int selected = responseSelector.select();
                if (selected > 0) {
                    handleSelectedResponses();
                }
                addClientsToResponse();
//                toUnregister.forEach(c -> c.socketChannel.keyFor(responseSelector).cancel()); // FIXME вот тут не работает тупо
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    private void addClientsToResponse() throws ClosedChannelException {
        if (!isWorking) return;
        Client client = toRegisterResponses.poll();
        while (client != null) {
            toUnregister.remove(client);;
            client.socketChannel.register(responseSelector, SelectionKey.OP_WRITE, client);
            client = toRegisterResponses.poll();
        }
    }

    private void handleSelectedResponses() {
        if (!isWorking) return;

        Set<SelectionKey> selectedKeys = responseSelector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            handleSelectionKeyToResponse(key);
            keyIterator.remove();
        };
    }

    private void handleSelectionKeyToResponse(SelectionKey key) {
        if (!isWorking) return;

        try {
            if (!key.isWritable()) throw new RuntimeException("Keys are supposed to be writable here");

            Client client = (Client) key.attachment();
            SocketChannel socketChannel = (SocketChannel) key.channel();

            client.responseBuffer.flip();
            socketChannel.write(client.responseBuffer);
            client.responseBuffer.compact();

            if (client.responseBuffer.hasRemaining()) {
                toUnregister.add(client);
            }
        } catch (IOException ignored) { }
    }

    private void acceptClients() {
        try (ServerSocketChannel ignored = serverSocketChannel) {
            while (!Thread.interrupted() && isWorking) {
                try {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    toRegisterRequests.add(socketChannel);
                    requestSelector.wakeup();
                } catch (IOException ignored1) { }
            }
        } catch (IOException ignored) { }
    }

    private void receive() {
        try {
            while (!Thread.interrupted() && isWorking) {
                int selected = requestSelector.select();
                if (selected > 0) {
                    handleSelectedRequests();
                }
                addClientsToReceive();
            }
        } catch (IOException ignored) { }
    }

    private void addClientsToReceive() throws IOException {
        if (!isWorking) return;

        SocketChannel socketChannel = toRegisterRequests.poll();
        while (socketChannel != null) {
            Client client = new Client(socketChannel);
            clients.add(client);

            socketChannel.configureBlocking(false);
            socketChannel.register(requestSelector, SelectionKey.OP_READ, client);
            socketChannel = toRegisterRequests.poll();
        }
    }

    private void handleSelectedRequests() {
        if (!isWorking) return;

        Set<SelectionKey> selectedKeys = requestSelector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            handleSelectionKeyToRequest(key);
            keyIterator.remove();
        };
    }

    private void handleSelectionKeyToRequest(SelectionKey key) {
        try {
            if (!key.isReadable()) throw new RuntimeException("Keys are supposed to be readable here");

            Client client = (Client) key.attachment();

            SocketChannel socketChannel = (SocketChannel) key.channel();
            socketChannel.read(client.requestBuffer);

            client.handleBuffer();
        } catch (IOException ignored) { }
    }

    private class Client {
        private final SocketChannel socketChannel;

        private final ByteBuffer requestBuffer = ByteBuffer.allocate(1024 * 16); // TODO
        private final ByteBuffer responseBuffer = ByteBuffer.allocate(1024 * 16); // TODO
        private int msgSize = -1;

        private volatile boolean working = true;

        private Client(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        public void handleBuffer() throws IOException {
            if (!working) return;

            requestBuffer.flip();

            if (msgSize == -1) {
                if (!tryReadSize()) return;
            } else {
                if (!tryReadMessage()) return;
            }

            requestBuffer.compact();
            handleBuffer();
        }

        private boolean tryReadSize() {
            if (requestBuffer.remaining() < 4) return false;
            msgSize = requestBuffer.getInt();
            return true;
        }

        private boolean tryReadMessage() throws IOException {
            if (requestBuffer.remaining() < msgSize) return false;
            byte[] buf = new byte[msgSize];
            requestBuffer.get(buf);
            msgSize = -1;

            handleMessage(buf);
            return true;
        }

        private void handleMessage(byte[] buf) throws IOException {
            if (!working) return;

            int[] data = Utils.readArray(buf);
            workerThreadPool.submit(() -> {
                bubbleSort(data);
                sendResponse(data);
            });
        }

        private void sendResponse(int[] data) {
            if (!working) return;

            byte[] toSend = Utils.serializeArray(data);
            responseBuffer.putInt(toSend.length);
            responseBuffer.put(toSend);

            toRegisterResponses.add(this);
            responseSelector.wakeup();
        }

        @Override
        public int hashCode() {
            return Objects.hash(socketChannel, requestBuffer, responseBuffer, msgSize);
        }

        public void close() {
            working = false;
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
