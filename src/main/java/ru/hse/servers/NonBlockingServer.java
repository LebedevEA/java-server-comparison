package ru.hse.servers;

import ru.hse.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.hse.utils.Constants.PORT;
import static ru.hse.utils.Utils.bubbleSort;

public class NonBlockingServer implements Server {
    private ServerSocketChannel serverSocketChannel = null;
    private Selector readSelector = null;

    private final ExecutorService serverSocketService = Executors.newSingleThreadExecutor();
    private final ExecutorService readSelectorService = Executors.newSingleThreadExecutor();
    private final ExecutorService writeSelectorService = Executors.newSingleThreadExecutor();
    private final ExecutorService workerThreadPool = Executors.newFixedThreadPool(16);


    private final Queue<SocketChannel> toRegister = new ConcurrentLinkedQueue<>();

    private volatile boolean isWorking = true;

    @Override
    public void start() throws IOException {
        serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(PORT));
        serverSocketService.submit(this::acceptClients);

        readSelector = Selector.open();
        readSelectorService.submit(this::handleClients);
    }

    private void acceptClients() {
        System.out.println("Client accept started");
        try (ServerSocketChannel ignored = serverSocketChannel) {
            while (!Thread.interrupted() && isWorking) {
//                System.out.println("Client accept cycle in");
                try {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    toRegister.add(socketChannel);
                    readSelector.wakeup();
//                    System.out.println("Selector woken up");
                } catch (IOException ignored1) { }
            }
        } catch (IOException ignored) { }
    }

    private void handleClients() {
        try {
            while (!Thread.interrupted() && isWorking) {
//                System.out.println("Selector selects");
                int selected = readSelector.select();
//                System.out.println("Selector selected " + selected);
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
//            System.out.println("Register socketChannel");
            socketChannel.configureBlocking(false);
            socketChannel.register(readSelector, SelectionKey.OP_READ, new Client()); // TODO save client somewhere
            socketChannel = toRegister.poll();
        }
    }

    private void handleSelected() {
        Set<SelectionKey> selectedKeys = readSelector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
//            System.out.println("Handle selection key");
            handleSelectionKey(key);
            keyIterator.remove();
        };
//        System.out.println("End handle selected");
    }

    private void handleSelectionKey(SelectionKey key) {
        try {
            System.out.println("Start handle selection key");
            if (!key.isReadable()) {
                throw new RuntimeException("Keys are supposed to be readable here");
            }
//            System.out.println("Start handle selection key");
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
        private final ByteBuffer readBuffer = ByteBuffer.allocate(1024 * 1024); // TODO
        private final ByteBuffer writeBuffer = ByteBuffer.allocate(1024 * 1024); // TODO
        private int msgSize = -1;

        public void handleBuffer() throws IOException {
            readBuffer.flip();

//            System.out.println("Handling buffer in client & msgSize is " + msgSize);

            if (msgSize != -1) {
                if (readBuffer.remaining() < msgSize) {
                    return;
                }

                byte[] buf = new byte[msgSize];
                readBuffer.get(buf);
                readBuffer.compact();
                msgSize = -1;

//                System.out.println("Sent to handleMessage");
                handleMessage(buf);
            } else {
                if (readBuffer.remaining() < 4) {
                    return;
                }

                msgSize = readBuffer.getInt();
                System.out.println("Int is red & it is " + msgSize);
                readBuffer.compact();
            }

            readBuffer.flip();
            handleBuffer();
        }

        private void handleMessage(byte[] buf) throws IOException {
            int[] data = Utils.readArray(new DataInputStream(new ByteArrayInputStream(buf)));
            System.out.println("Before submit to wtp");
            workerThreadPool.submit(() -> {
                System.out.println("Worker starts");
                bubbleSort(data);
                sendResponse(data);
            });
        }

        private void sendResponse(int[] data) {
            System.out.println("Wants to send");
        }
    }
}
