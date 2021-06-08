package ru.hse;

import org.junit.jupiter.api.Test;
import ru.hse.utils.Utils;

import java.util.Arrays;
import java.util.Random;

import static java.util.Arrays.sort;
import static ru.hse.utils.Utils.bubbleSort;
import static org.junit.jupiter.api.Assertions.*;

class UtilTest {
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

    private int[] randomIntArray(Random random) {
        int[] array = new int[random.nextInt(1000)];

        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(100_000);
        }

        return array;
    }
}