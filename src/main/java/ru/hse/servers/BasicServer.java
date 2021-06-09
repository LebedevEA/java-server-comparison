package ru.hse.servers;

import ru.hse.utils.Utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.hse.utils.Constants.PORT;
import static ru.hse.utils.Utils.bubbleSort;

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
                    Client client = new Client(clientSocket);
                    clients.add(client);
                    client.processClient();
                } catch (IOException ignored1) { }
            }
        } catch (IOException ignored) { }
    }

    private class Client {
        private final Socket socket;
        public final ExecutorService responseWriter = Executors.newSingleThreadExecutor();
        public final ExecutorService requestReader = Executors.newSingleThreadExecutor();

        private final DataInputStream inputStream;
        private final DataOutputStream outputStream;

        private volatile boolean working = true;

        private Client(Socket socket) throws IOException {
            this.socket = socket;
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        }

        public void sendResponse(int[] data) {
            responseWriter.submit(() -> {
                try {
                    Utils.writeArray(outputStream, data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        public void processClient() {
            requestReader.submit(() -> {
                try {
                    while(!Thread.interrupted() && working) {
                        int[] data = Utils.readArray(inputStream);
                        workerThreadPool.submit(() -> {
                            bubbleSort(data);
                            sendResponse(data);
                        });
                    }
                } catch (IOException ignored) { }
            });
        }


        public void close() {
            working = false;
            responseWriter.shutdown();
            requestReader.shutdown();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Client that = (Client) o;
            return working == that.working && Objects.equals(socket, that.socket)
                    && Objects.equals(responseWriter, that.responseWriter)
                    && Objects.equals(requestReader, that.requestReader)
                    && Objects.equals(inputStream, that.inputStream)
                    && Objects.equals(outputStream, that.outputStream);
        }

        @Override
        public int hashCode() {
            return Objects.hash(socket, responseWriter, requestReader, inputStream, outputStream, working);
        }
    }
}
