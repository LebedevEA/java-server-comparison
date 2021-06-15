package ru.hse.servers.blocking;

import ru.hse.servers.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.hse.utils.Constants.PORT;
import static ru.hse.utils.Constants.THREADS;

public class BlockingServer extends Server {
    private ServerSocket serverSocket = null;

    private final ExecutorService serverSocketService = Executors.newSingleThreadExecutor();
    private final ExecutorService workerThreadPool = Executors.newFixedThreadPool(THREADS);

    private volatile boolean isWorking = true;

    private final Set<BlockingClientHandler> clients = ConcurrentHashMap.newKeySet();

    @Override
    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        serverSocketService.submit(this::acceptClients);
    }

    @Override
    public void stop() {
        try {
            isWorking = false;
            serverSocket.close();
            workerThreadPool.shutdown();
            serverSocketService.shutdown();
            clients.forEach(BlockingClientHandler::close);
        } catch (IOException ignored) { }
    }

    private void acceptClients() {
        try (ServerSocket ignored = serverSocket) {
            while (!Thread.interrupted() && isWorking) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    BlockingClientHandler client = new BlockingClientHandler(
                            clientSocket,
                            workerThreadPool,
                            this::addWorkTime
                    );
                    clients.add(client);
                    client.processClient();
                } catch (IOException ignored1) { }
            }
        } catch (IOException ignored) { }
    }
}
