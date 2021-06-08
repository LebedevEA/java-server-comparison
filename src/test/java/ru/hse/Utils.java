package ru.hse;

import java.util.Random;

public class Utils {
    public static int[] randomIntArray(Random random) {
        int[] array = new int[random.nextInt(1000)];

        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(100_000);
        }

        return array;
    }
}
