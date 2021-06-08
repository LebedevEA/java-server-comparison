package ru.hse.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

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
        // TODO
    }

    public static int[] readArray(DataInputStream inputStream) throws IOException {
        return new int[0]; // TODO
    }
}
