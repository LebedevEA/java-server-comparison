package ru.hse.utils;

import ru.hse.utils.protocols.Array;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static void writeArray(DataOutputStream outputStream, int[] data) throws IOException {
        var array = Array.newBuilder()
                .addAllArray(
                        Arrays.stream(data)
                                .boxed().
                                collect(Collectors.toList())
                )
                .build();
        byte[] buf = array.toByteArray();
        outputStream.writeInt(buf.length);
        outputStream.write(buf);
    }

    public static int[] readArray(DataInputStream inputStream) throws IOException {
        int bufLen = inputStream.readInt();
        byte[] buf = new byte[bufLen];
        inputStream.readFully(buf);

        return Array.parseFrom(buf).getArrayList().stream().mapToInt(i -> i).toArray();
    }

    public static int[] randomIntArray(Random random, int size) {
        int[] array = new int[size];

        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(100_000);
        }

        return array;
    }
}
