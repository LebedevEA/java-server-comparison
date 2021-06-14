package ru.hse.servers.basic;

import ru.hse.servers.Server;
import ru.hse.utils.Utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.hse.utils.Constants.PORT;

public class BasicServer implements Server {
    private ServerSocket serverSocket = null;

    private final ExecutorService serverSocketService = Executors.newSingleThreadExecutor();
    private final ExecutorService workerThreadPool = Executors.newFixedThreadPool(16);

    private volatile boolean isWorking = true;

    private final ConcurrentHashMap.KeySetView<Client, Boolean> clients = ConcurrentHashMap.newKeySet();

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
            clients.forEach(Client::close);
        } catch (IOException ignored) { }
    }

    private void acceptClients() {
        try (ServerSocket ignored = serverSocket) {
            while (!Thread.interrupted() && isWorking) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    Client client = new Client(clientSocket, workerThreadPool);
                    clients.add(client);
                    client.processClient();
                } catch (IOException ignored1) { }
            }
        } catch (IOException ignored) { }
    }
}
