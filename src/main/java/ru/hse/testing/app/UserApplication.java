package ru.hse.testing.app;

import ru.hse.testing.stats.Result;
import ru.hse.testing.stats.TestingState;

import java.util.List;

import static ru.hse.utils.Utils.runTesting;

public class UserApplication {
    public static void main(String[] args) throws Exception {
        Cli cli = new Cli();
        if (!cli.collectData()) {
            return;
        }
        TestingState state = cli.getState();
        List<Result> results = runTesting(state);
        System.out.println(results);
    }
}
