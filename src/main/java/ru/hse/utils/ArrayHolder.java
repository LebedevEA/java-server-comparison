package ru.hse.utils;

public class ArrayHolder {
    private final int id;
    private final int[] array;

    public ArrayHolder(int id, int[] array) {
        this.id = id;
        this.array = array;
    }

    public int getId() {
        return id;
    }

    public int[] getArray() {
        return array;
    }
}
