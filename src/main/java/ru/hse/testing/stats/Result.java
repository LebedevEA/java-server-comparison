package ru.hse.testing.stats;

import ru.hse.servers.Server;

import java.util.List;
import java.util.function.Supplier;

public class Result {
    private final Supplier<Server> architecture;
    private final int dataLength; // aka N
    private final int clientNumber; // aka M
    private final int queryWaitTime; // aka ∆
    private final int queryNumberPerClient; // aka X
    private final int queryWorkTimeOnServer;
    private final int queryWaitTimeOnClient;

    public Result(TestingState state, List<Integer> queryWorkTimeOnServer, List<Integer> queryWaitTimeOnClient) {
        architecture = state.getArchitecture();
        dataLength = state.getDataLength();
        clientNumber = state.getClientNumber();
        queryWaitTime = state.getQueryWaitTime();
        queryNumberPerClient = state.getQueryNumberPerClient();

        long sum = queryWorkTimeOnServer.stream().mapToLong(i -> i).sum() / queryWorkTimeOnServer.size();
        this.queryWorkTimeOnServer = (int) sum;
        sum = queryWaitTimeOnClient.stream().mapToLong(i -> i).sum() / queryWaitTimeOnClient.size();
        this.queryWaitTimeOnClient = (int) sum;
    }
}
