package ru.hse.servers;

import ru.hse.utils.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.hse.utils.Utils.*;

public class TestClient  implements Callable<ArrayTestPair> {
    Random random = new Random(123);
    private final static AtomicInteger counter = new AtomicInteger();

    @Override
    public ArrayTestPair call() throws Exception {
//        int cnt = counter.incrementAndGet();
//        System.out.println("Client Socket Before Create " + cnt);

        int[] data = randomIntArray(random, Constants.SIZE);
        int[] sortedData;

        try (Socket socket = new Socket("localhost", Constants.PORT)) {
//            System.out.println("Client Socket Created " + cnt);
            var inputStream = new DataInputStream(socket.getInputStream());
            var outputStream = new DataOutputStream(socket.getOutputStream());
            writeArray(outputStream, data);
            sortedData = readArray(inputStream);
        }

        return new ArrayTestPair(data, sortedData);
    }
}
