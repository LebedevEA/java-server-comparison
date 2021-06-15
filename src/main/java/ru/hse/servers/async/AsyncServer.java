package ru.hse.servers.async;

import ru.hse.servers.Server;
import ru.hse.utils.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncServer implements Server {
    private final ExecutorService workerThreadPool = Executors.newFixedThreadPool(16);
    private AsynchronousServerSocketChannel serverSocketChannel = null;

    private volatile boolean isWorking = true;

    private final Set<Client> clients = ConcurrentHashMap.newKeySet();

    @Override
    public void start() throws IOException {
        serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(Constants.PORT));
        serverSocketChannel.accept(
                null,
                new CompletionHandler<>() {
                    @Override
                    public void completed(AsynchronousSocketChannel result, Object attachment) {
                        if (!isWorking) return;
                        handleSocketChannel(result);
                        serverSocketChannel.accept(null, this);
                    }

                    @Override
                    public void failed(Throwable ignored, Object attachment) { }
                }
        );
    }

    private void handleSocketChannel(AsynchronousSocketChannel socketChannel) {
        Client client = new Client(socketChannel, workerThreadPool);
        clients.add(client);
        client.run();
    }

    @Override
    public void stop() throws IOException {
        isWorking = false;
        serverSocketChannel.close();
        workerThreadPool.shutdown();
        clients.forEach(Client::stop);
    }

    public static class ClientBufferWrapper {
        private final Client client;
        private final ByteBuffer buffer;

        public ClientBufferWrapper(Client client, ByteBuffer buffer) {
            this.client = client;
            this.buffer = buffer;
        }

        public Client getClient() {
            return client;
        }

        public ByteBuffer getBuffer() {
            return buffer;
        }
    }
}