package ru.hse.testing.testers;

import ru.hse.utils.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.hse.utils.Utils.*;

public class Client implements Callable<Void> {
    private final AtomicInteger idGetter = new AtomicInteger();

    private final Random random;
    private final int size;

    public Client(Random random, int size) {
        this.random = random;
        this.size = size;
    }

    @Override
    public Void call() throws Exception {
        int[] data = randomIntArray(random, size);

        try (Socket socket = new Socket("localhost", Constants.PORT)) {
            var inputStream = new DataInputStream(socket.getInputStream());
            var outputStream = new DataOutputStream(socket.getOutputStream());
//            writeArray(outputStream, data);
//            writeArray(outputStream, data);
//            readArray(inputStream);
//            readArray(inputStream);
        }

        return null;
    }
}