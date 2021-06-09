package ru.hse.servers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.hse.utils.Constants.PORT;

public class NonBlockingServer implements Server {
    private ServerSocketChannel serverSocketChannel = null;
    private Selector selector = null;

    private final ExecutorService serverSocketService = Executors.newSingleThreadExecutor();
    private final ExecutorService selectorService = Executors.newSingleThreadExecutor();

    private final Queue<SocketChannel> toRegister = new ConcurrentLinkedQueue<>();

    private volatile boolean isWorking = true;

    @Override
    public void start() throws IOException {
        serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(PORT));
        serverSocketService.submit(this::acceptClients);

        selector = Selector.open();
        selectorService.submit(this::handleClients);
    }

    private void acceptClients() {
        System.out.println("Client accept started");
        try (ServerSocketChannel ignored = serverSocketChannel) {
            while (!Thread.interrupted() && isWorking) {
                System.out.println("Client accept cycle in");
                try {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    toRegister.add(socketChannel);
                    selector.wakeup();
                    System.out.println("Selector woken up");
                } catch (IOException ignored1) { }
            }
        } catch (IOException ignored) { }
    }

    private void handleClients() {
        try {
            while (!Thread.interrupted() && isWorking) {
                System.out.println("Selector selects");
                int selected = selector.select();
                System.out.println("Selector selected " + selected);
                if (selected > 0) {
                    handleSelected();
                }
                addClients();
            }
        } catch (IOException ignored) { }
    }

    private void addClients() throws IOException {
        SocketChannel socketChannel = toRegister.poll();
        while (socketChannel != null) {
            System.out.println("Register socketChannel");
            socketChannel.configureBlocking(false);
            SelectionKey key = socketChannel.register(
                    selector,
                    SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                    new Client()
            );
            socketChannel = toRegister.poll();
        }
    }

    private void handleSelected() {
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            System.out.println("Handle selection key");
            handleSelectionKey(key);
            keyIterator.remove();
        };
        System.out.println("End handle selected");
    }

    private void handleSelectionKey(SelectionKey key) {
        try {
            System.out.println("Start handle selection key");
            if (!key.isReadable()) {
                throw new RuntimeException("Keys are supposed to be readable here");
            }
            System.out.println("Start handle selection key");
            Client client = (Client) key.attachment();

            SocketChannel socketChannel = (SocketChannel) key.channel();
            int DEBUG = socketChannel.write(client.readBuffer);

            System.out.println(DEBUG);

            client.handleBuffer();
        } catch (IOException ignored) { }
    }

    @Override
    public void stop() throws IOException {

    }

    private class Client {
        private final ByteBuffer readBuffer = ByteBuffer.allocate(1024); // TODO

        public void readIntoBuffer() {
        }

        public void check() {

        }

        public void handleBuffer() {

        }
    }
}
