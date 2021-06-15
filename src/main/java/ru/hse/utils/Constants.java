package ru.hse.utils;

import static java.lang.Math.max;

public class Constants {
    private Constants() { }

    public static final int THREADS = max(1, Runtime.getRuntime().availableProcessors() - 2);
    public static final int PORT = 3561;
    public static final int SIZE = 1000;
}
