package ru.hse.servers.async;

import ru.hse.servers.Server;
import ru.hse.utils.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.hse.utils.Constants.THREADS;

public class AsyncServer extends Server {
    private final ExecutorService workerThreadPool = Executors.newFixedThreadPool(THREADS);
    private AsynchronousServerSocketChannel serverSocketChannel = null;

    private volatile boolean isWorking = true;

    private final Set<AsyncClientHandler> clients = ConcurrentHashMap.newKeySet();

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
        AsyncClientHandler client = new AsyncClientHandler(
                socketChannel,
                workerThreadPool,
                this::addWorkTime
        );
        clients.add(client);
        client.run();
    }

    @Override
    public void stop() throws IOException {
        isWorking = false;
        serverSocketChannel.close();
        workerThreadPool.shutdown();
        clients.forEach(AsyncClientHandler::stop);
    }
}
