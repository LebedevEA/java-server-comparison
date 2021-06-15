package ru.hse.utils;

import org.junit.jupiter.api.Test;
import ru.hse.TestUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

import static java.util.Arrays.sort;
import static ru.hse.utils.Utils.*;
import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {
    @Test
    public void testBubbleSort() {
        Random random = new Random(5338);
        for (int i = 0; i < 1000; i++) {
            int[] arr1 = TestUtils.randomIntArray(random);
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

            writeArray(outputStream, arr1, 1);
            writeArray(outputStream, arr2, 2);
            writeArray(outputStream, arr3, 3);

            byte[] buf = out.toByteArray();

            try (ByteArrayInputStream in = new ByteArrayInputStream(buf)) {
                DataInputStream inputStream = new DataInputStream(in);

                var r1 = readArray(inputStream);
                var r2 = readArray(inputStream);
                var r3 = readArray(inputStream);

                assertArrayEquals(arr1, r1.getArray());
                assertArrayEquals(arr2, r2.getArray());
                assertArrayEquals(arr3, r3.getArray());

                assertEquals(1, r1.getId());
                assertEquals(2, r2.getId());
                assertEquals(3, r3.getId());

                assertThrows(
                        EOFException.class,
                        () -> readArray(inputStream)
                );
            }
        }
    }
}