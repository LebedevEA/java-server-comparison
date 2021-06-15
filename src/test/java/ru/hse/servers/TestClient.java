package ru.hse.servers;

import ru.hse.utils.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Callable;

import static ru.hse.utils.Utils.*;

public class TestClient implements Callable<ArrayTestPair> {
    private final Random random;

    public TestClient(Random random) {
        this.random = random;
    }

    @Override
    public ArrayTestPair call() throws Exception {
        int[] data = randomIntArray(random, Constants.SIZE);
        int[] sortedData;

        try (Socket socket = new Socket("localhost", Constants.PORT)) {
            var inputStream = new DataInputStream(socket.getInputStream());
            var outputStream = new DataOutputStream(socket.getOutputStream());
            writeArray(outputStream, data, 0);
            writeArray(outputStream, data, 0);
            readArray(inputStream);
            sortedData = readArray(inputStream).getArray();
        }

        return new ArrayTestPair(data, sortedData);
    }
}
