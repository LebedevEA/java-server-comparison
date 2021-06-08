package ru.hse;

import ru.hse.utils.Utils;

import java.util.Random;

public class TestUtils {
    private TestUtils() { }
    public static int[] randomIntArray(Random random) {
        return Utils.randomIntArray(random, random.nextInt(1000));
    }
}
