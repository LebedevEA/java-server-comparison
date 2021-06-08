package ru.hse.utils;

import org.junit.jupiter.api.Test;
import ru.hse.utils.Utils;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

import static java.util.Arrays.sort;
import static ru.hse.Utils.randomIntArray;
import static ru.hse.utils.Utils.*;
import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {
    @Test
    public void testBubbleSort() {
        Random random = new Random(5338);
        for (int i = 0; i < 1000; i++) {
            int[] arr1 = randomIntArray(random);
            int[] arr2 = arr1.clone();

            sort(arr1);
            bubbleSort(arr2);

            assertArrayEquals(arr1, arr2);
        }
    }

    @Test
    public void testReadWriteArray() throws IOException {
        final int[] arr1 = { 1, 2, 3 };
        final int[] arr2 = { 1, 2, 3, 4 };
        final int[] arr3 = { 1, 2, 3, 4, 5 };

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
            DataOutputStream outputStream = new DataOutputStream(out);

            writeArray(outputStream, arr1);
            writeArray(outputStream, arr2);
            writeArray(outputStream, arr3);

            byte[] buf = out.toByteArray();

            try (ByteArrayInputStream in = new ByteArrayInputStream(buf)) {
                DataInputStream inputStream = new DataInputStream(in);

                assertArrayEquals(arr1, readArray(inputStream));
                assertArrayEquals(arr2, readArray(inputStream));
                assertArrayEquals(arr3, readArray(inputStream));

                assertThrows(
                        EOFException.class,
                        () -> readArray(inputStream)
                );
            }
        }
    }
}