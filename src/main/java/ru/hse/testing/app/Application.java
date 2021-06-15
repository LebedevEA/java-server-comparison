package ru.hse.testing.app;

import ru.hse.testing.stats.Result;
import ru.hse.testing.stats.TestingState;

import java.util.ArrayList;
import java.util.List;

public class Application {
    public static void main(String[] args) {
        Cli cli = new Cli();
        if (!cli.collectData()) {
            return;
        }
        TestingState state = cli.getState();
        List<Result> results = runTesting(state);
    }

    private static List<Result> runTesting(TestingState state) {
        List<Result> rv = new ArrayList<>();
        while (state.isValid()) {
            rv.add(state.runTest());
            state.makeStep();
        }
        return rv;
    }
}
