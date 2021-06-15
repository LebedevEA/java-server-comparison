package ru.hse.testing.app;

import ru.hse.servers.Server;
import ru.hse.servers.async.AsyncServer;
import ru.hse.servers.blocking.BlockingServer;
import ru.hse.servers.nonblocking.NonBlockingServer;
import ru.hse.testing.stats.Parameter;
import ru.hse.testing.stats.Result;
import ru.hse.testing.stats.TestingState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ru.hse.utils.Utils.runTesting;

public class TotalDataCollector {
    public static void main(String[] args) throws IOException {
        writeChangingDataLength();
        writeChangingClientNumber();
        writeChangingQueryWaitTime();
    }

    private static void writeChangingDataLength() throws IOException {
        Map<String, List<Result>> resultMap = Arrays.stream(NAMES).collect(Collectors.toMap(
                name -> name,
                name -> {
                    var state = makeTestingStateChangingDataLength(name);
                    try {
                        System.out.println("Starting testing (length): " + name);
                        return runTesting(state);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        ));
        System.out.println("Writing to CSV");
        writeCSV(resultMap, Parameter.DATA_LENGTH);
    }

    private static void writeChangingClientNumber() throws IOException {
        Map<String, List<Result>> resultMap = Arrays.stream(NAMES).collect(Collectors.toMap(
                name -> name,
                name -> {
                    var state = makeTestingStateChangingClientNumber(name);
                    try {
                        System.out.println("Starting testing (client): " + name);
                        return runTesting(state);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        ));
        System.out.println("Writing to CSV");
        writeCSV(resultMap, Parameter.CLIENT_NUMBER);
    }

    private static void writeChangingQueryWaitTime() throws IOException {
        Map<String, List<Result>> resultMap = Arrays.stream(NAMES).collect(Collectors.toMap(
                name -> name,
                name -> {
                    var state = makeTestingStateChangingQueryWaitTime(name);
                    try {
                        System.out.println("Starting testing (wait): " + name);
                        return runTesting(state);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        ));
        System.out.println("Writing to CSV");
        writeCSV(resultMap, Parameter.QUERY_WAIT_TIME);
    }

    private static final String CSV_HEADER = "Length, Clients, Wait, Queries, Server works, Mean client waits";

    private static void writeCSV(Map<String, List<Result>> resultMap, Parameter changing) throws IOException {
        Path dir = Paths.get(".", DIR, changing.toString());
        Files.createDirectories(dir);

        for (var entry : resultMap.entrySet()) {
            Path path = dir.resolve(entry.getKey() + ".csv");
            try (var out = Files.newBufferedWriter(path)) {
                out.write(CSV_HEADER);
                out.newLine();
                for (var result : entry.getValue()) {
                    out.write(
                            result.getDataLength() + ", " +
                            result.getClientNumber() + ", " +
                            result.getQueryWaitTime() + ", " +
                            result.getQueryNumberPerClient() + ", " +
                            result.getQueryWorkTimeOnServer() + ", " +
                            result.getQueryWaitTimeOnClient()
                    );
                    out.newLine();
                }
            }
        }
    }

    private static TestingState makeTestingStateChangingDataLength(String name) {
        TestingState state = TestingState.empty();
        state.setArchitecture(NAMES_TO_SUPPLIER.get(name));
        state.setQueryNumberPerClient(QUERY_NUMBER_PER_CLIENT);

        state.setChanging(Parameter.DATA_LENGTH);
        state.setClientNumber(DEFAULT_CLIENT_NUMBER);
        state.setQueryWaitTime(DEFAULT_QUERY_WAIT_TIME);

        state.setStep(LENGTH_STEP);
        state.setLowerLimit(LENGTH_LOWER);
        state.setUpperBound(LENGTH_UPPER);

        return state;
    }

    private static TestingState makeTestingStateChangingClientNumber(String name) {
        TestingState state = TestingState.empty();
        state.setArchitecture(NAMES_TO_SUPPLIER.get(name));
        state.setQueryNumberPerClient(QUERY_NUMBER_PER_CLIENT);

        state.setChanging(Parameter.CLIENT_NUMBER);
        state.setDataLength(DEFAULT_DATA_LENGTH);
        state.setQueryWaitTime(DEFAULT_QUERY_WAIT_TIME);

        state.setStep(CLIENT_STEP);
        state.setLowerLimit(CLIENT_LOWER);
        state.setUpperBound(CLIENT_UPPER);

        return state;
    }

    private static TestingState makeTestingStateChangingQueryWaitTime(String name) {
        TestingState state = TestingState.empty();
        state.setArchitecture(NAMES_TO_SUPPLIER.get(name));
        state.setQueryNumberPerClient(QUERY_NUMBER_PER_CLIENT);

        state.setChanging(Parameter.QUERY_WAIT_TIME);
        state.setDataLength(DEFAULT_DATA_LENGTH);
        state.setClientNumber(DEFAULT_CLIENT_NUMBER);

        state.setStep(WAIT_STEP);
        state.setLowerLimit(WAIT_LOWER);
        state.setUpperBound(WAIT_UPPER);

        return state;
    }

    private static final int QUERY_NUMBER_PER_CLIENT = 10;

    private static final int DEFAULT_DATA_LENGTH = 1250;
    private static final int DEFAULT_CLIENT_NUMBER = 20;
    private static final int DEFAULT_QUERY_WAIT_TIME = 250;

    private static final String DIR = "data";

    private static final String[] NAMES = { "Blocking", "NonBlocking", "Async" };
    private static final Map<String, Supplier<Server>> NAMES_TO_SUPPLIER = Map.of(
            "Blocking", BlockingServer::new,
            "NonBlocking", NonBlockingServer::new,
            "Async", AsyncServer::new
    );

    private static final int LENGTH_STEP = 200;
    private static final int LENGTH_LOWER = 800;
    private static final int LENGTH_UPPER = 3000;

    private static final int CLIENT_STEP = 5;
    private static final int CLIENT_LOWER = 5;
    private static final int CLIENT_UPPER = 85;

    private static final int WAIT_STEP = 25;
    private static final int WAIT_LOWER = 5;
    private static final int WAIT_UPPER = 500;
}
