package ru.hse.servers.nonblocking;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;

public class RequestHandler {
    private final Selector selector = Selector.open();
    private final Queue<NonBlockingClientHandler> newClients;

    private volatile boolean isWorking = true;

    public RequestHandler(Queue<NonBlockingClientHandler> addToRequests) throws IOException {
        this.newClients = addToRequests;
    }

    public void wakeup() {
        selector.wakeup();
    }

    void run() {
        try (Selector ignored = selector) {
            while (!Thread.interrupted() && isWorking) {
                int selected = selector.select();
                if (selected > 0) {
                    handleSelected();
                }
                addNew();
            }
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException ignored) { }
    }

    private void handleSelected() {
        var selectedKeys = selector.selectedKeys();
        var keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
            var key = keyIterator.next();
            if (!key.isReadable()) throw new RuntimeException("Keys are supposed to be readable here");
            handleSelectorKey(key);
            keyIterator.remove();
        }
    }

    private void handleSelectorKey(SelectionKey key) {
        NonBlockingClientHandler client = (NonBlockingClientHandler) key.attachment();
        client.read();
//        if (client.done()) {
//            key.cancel();
//        }
    }

    private void addNew() throws IOException {
        NonBlockingClientHandler client = newClients.poll();
        while (client != null) {
            client.getSocketChannel().configureBlocking(false);
            client.getSocketChannel().register(selector, SelectionKey.OP_READ, client);
            client = newClients.poll();
        }
    }

    public void close() {
        try {
            isWorking = false;
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
