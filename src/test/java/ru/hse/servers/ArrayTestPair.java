package ru.hse.servers;

import org.junit.jupiter.api.Assertions;

import static java.util.Arrays.sort;

public class ArrayTestPair {
    private final int[] initial;
    private final int[] toCheck;

    public ArrayTestPair(int[] initial, int[] toCheck) {
        this.initial = initial;
        this.toCheck = toCheck;
    }

    public void check() {
        int[] expected = initial.clone();
        sort(expected);
        Assertions.assertArrayEquals(expected, toCheck);
    }
}
