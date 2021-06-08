package ru.hse.clients;

import ru.hse.utils.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Callable;

import static ru.hse.utils.Utils.*;

public class BasicClient implements Callable<int[]> {
    Random random = new Random(123);

    @Override
    public int[] call() throws Exception {
        int[] data = randomIntArray(random, Constants.SIZE);
        int[] sortedData;

        try (Socket socket = new Socket("localhost", Constants.PORT);) {
            var inputStream = new DataInputStream(socket.getInputStream());
            var outputStream = new DataOutputStream(socket.getOutputStream());
            writeArray(outputStream, data);
            sortedData = readArray(inputStream);
        }

        return sortedData;
    }
}