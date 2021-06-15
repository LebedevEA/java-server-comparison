package ru.hse.testing.testers;

import ru.hse.utils.ArrayHolder;
import ru.hse.utils.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.hse.utils.Utils.*;

public class Client implements Callable<Void> {
    private final ExecutorService writeService = Executors.newSingleThreadExecutor();
    private final ExecutorService readService = Executors.newSingleThreadExecutor();

    private final Map<Integer, Long> writeTimes = new HashMap<>();
    private final Map<Integer, Long> readTimes = new HashMap<>();

    private int idCounter = 0;

    private final Random random;
    private final int size;
    private final int wait;
    private final int queries;

    private final AtomicBoolean isAnyDone;

    public Client(Random random, int size, int wait, int queries, AtomicBoolean isAnyDone) {
        this.random = random;
        this.size = size;
        this.wait = wait;
        this.queries = queries;
        this.isAnyDone = isAnyDone;
    }

    @Override
    public Void call() throws Exception {
        try (Socket socket = new Socket("localhost", Constants.PORT)) {
            var inputStream = new DataInputStream(socket.getInputStream());
            var outputStream = new DataOutputStream(socket.getOutputStream());
            var writes = writeService.submit(() -> {
                for (int i = 0; i < queries; i++) {
                    int currentId = idCounter++;
                    int[] data = randomIntArray(random, size);
                    try {
                        writeArray(outputStream, data, currentId);
                        writeTimes.put(currentId, System.currentTimeMillis());
                        if (!isAnyDone.get()) {
                            TimeUnit.MILLISECONDS.sleep(wait);
                        }
                    } catch (IOException | InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            });
            var reads = readService.submit(() -> {
                for (int i = 0; i < queries; i++) {
                    try {
                        ArrayHolder arrayHolder = readArray(inputStream);
                        if (!isAnyDone.get()) {
                            readTimes.put(arrayHolder.getId(), System.currentTimeMillis());
                        }
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                }
                isAnyDone.set(true);
            });

            writes.get();
            reads.get();

            writeService.shutdown();
            readService.shutdown();
        }

        return null;
    }

    List<Integer> getWaitTimes() {
        List<Integer> waits = new ArrayList<>();
        readTimes.forEach((key, value) -> {
            if (value != null) {
                waits.add((int) (value - writeTimes.get(key)));
            }
        });
        return waits;
    }
}