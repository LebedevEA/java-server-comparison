package ru.hse.servers.blocking;

import ru.hse.utils.ArrayHolder;
import ru.hse.utils.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static ru.hse.utils.Utils.bubbleSort;

public class BlockingClientHandler {
    private final Socket socket;
    private final ExecutorService workerThreadPool;
    private final Consumer<Integer> addWorkTime;
    private final ExecutorService responseWriter = Executors.newSingleThreadExecutor();
    private final ExecutorService requestReader = Executors.newSingleThreadExecutor();

    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    private volatile boolean working = true;

    public BlockingClientHandler(
            Socket socket,
            ExecutorService workerThreadPool,
            Consumer<Integer> addWorkTime
    ) throws IOException {
        this.socket = socket;
        this.workerThreadPool = workerThreadPool;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        this.addWorkTime = addWorkTime;
    }

    private void sendResponse(int[] data, int id) {
        responseWriter.submit(() -> {
            try {
                Utils.writeArray(outputStream, data, id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void processClient() {
        requestReader.submit(() -> {
            try {
                while(!Thread.interrupted() && working) {
                    ArrayHolder data = Utils.readArray(inputStream);
                    workerThreadPool.submit(() -> {
                        long start = System.currentTimeMillis();
                        bubbleSort(data.getArray());
                        long stop = System.currentTimeMillis();
                        addWorkTime.accept((int) (stop - start));
                        sendResponse(data.getArray(), data.getId());
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