package ru.hse.servers.basic;

import ru.hse.utils.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.hse.utils.Utils.bubbleSort;

public class BasicClientHandler {
    private final Socket socket;
    private final ExecutorService workerThreadPool;
    private final ExecutorService responseWriter = Executors.newSingleThreadExecutor();
    private final ExecutorService requestReader = Executors.newSingleThreadExecutor();

    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    private volatile boolean working = true;

    public BasicClientHandler(Socket socket, ExecutorService workerThreadPool) throws IOException {
        this.socket = socket;
        this.workerThreadPool = workerThreadPool;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    private void sendResponse(int[] data) {
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
    public int hashCode() {
        return Objects.hash(socket, responseWriter, requestReader, inputStream, outputStream, working);
    }
}