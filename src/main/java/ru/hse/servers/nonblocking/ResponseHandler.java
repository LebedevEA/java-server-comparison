package ru.hse.servers.nonblocking;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;

public class ResponseHandler {
    private final Selector selector = Selector.open();
    private final Queue<NonBlockingClientHandler> newClients;

    private volatile boolean isWorking = true;

    public ResponseHandler(Queue<NonBlockingClientHandler> addToResponses) throws IOException {
        this.newClients = addToResponses;
    }

    public void wakeup() {
        selector.wakeup();
    }

    public void run() {
        try (Selector ignored = selector) {
            while (!Thread.interrupted() && isWorking) {
                int selected = selector.select();
                if (selected > 0) {
                    handleSelected();
                }
                addNew();
            }
        } catch (IOException ignored) { }
    }

    private void handleSelected() {
        var selectedKeys = selector.selectedKeys();
        var keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
            var key = keyIterator.next();
            if (!key.isWritable()) throw new RuntimeException("Keys are supposed to be writeable here");
            handleSelectorKey(key);
            keyIterator.remove();
        }
    }

    private void handleSelectorKey(SelectionKey key) {
        try {
            NonBlockingClientHandler client = (NonBlockingClientHandler) key.attachment();
            client.write();
            if (!client.wantsWrite()) {
                key.interestOps(0);
            }
        } catch (IOException ignored) { }
    }

    private void addNew() throws ClosedChannelException {
        NonBlockingClientHandler client = newClients.poll();
        while (client != null) {
            client.getSocketChannel().register(selector, SelectionKey.OP_WRITE, client);
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
