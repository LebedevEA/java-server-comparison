package ru.hse.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.hse.testing.stats.Result;
import ru.hse.testing.stats.TestingState;
import ru.hse.utils.protocols.Array;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Utils {
    private Utils() { }

    public static void bubbleSort(final int[] array) {
        for (int i = array.length - 1; i >= 0; i--) {
            for (int j = 0; j < i; j++) {
                trySwap(array, j);
            }
        }
    }

    private static void trySwap(final int[] array, int index) {
        if (array[index] > array[index + 1]) {
            int tmp = array[index + 1];
            array[index + 1] = array[index];
            array[index] = tmp;
        }
    }

    public static void writeArray(DataOutputStream outputStream, int[] data, int id) throws IOException {
        byte[] buf = serializeArray(data, id);
        outputStream.writeInt(buf.length);
        outputStream.write(buf);
    }

    public static ArrayHolder readArray(DataInputStream inputStream) throws IOException {
        int bufLen = inputStream.readInt();
        byte[] buf = new byte[bufLen];
        inputStream.readFully(buf);

        return readArray(buf);
    }

    public static byte[] serializeArray(int[] data, int id) {
        var array = Array.newBuilder()
                .setId(id)
                .addAllArray(
                        Arrays.stream(data)
                                .boxed().
                                collect(Collectors.toList())
                )
                .build();
        return array.toByteArray();
    }

    public static ArrayHolder readArray(byte[] data) throws InvalidProtocolBufferException {
        Array array = Array.parseFrom(data);
        return new ArrayHolder(array.getId(), array.getArrayList().stream().mapToInt(i -> i).toArray());
    }

    public static int[] randomIntArray(Random random, int size) {
        int[] array = new int[size];

        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(100_000);
        }

        return array;
    }

    public static List<Result> runTesting(TestingState state) throws Exception {
        List<Result> rv = new ArrayList<>();
        while (state.isValid()) {
            rv.add(state.runTest());
            state.makeStep();
        }
        return rv;
    }
}
