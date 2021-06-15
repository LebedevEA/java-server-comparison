package ru.hse.testing.stats;

import java.util.List;

public class Result {
    private final int dataLength; // aka N
    private final int clientNumber; // aka M
    private final int queryWaitTime; // aka ∆
    private final int queryNumberPerClient; // aka X
    private final double queryWorkTimeOnServer;
    private final double queryWaitTimeOnClient;

    public Result(TestingState state, double queryWorkTimeOnServer, List<Integer> queryWaitTimeOnClient) {
        dataLength = state.getDataLength();
        clientNumber = state.getClientNumber();
        queryWaitTime = state.getQueryWaitTime();
        queryNumberPerClient = state.getQueryNumberPerClient();

        this.queryWorkTimeOnServer = queryWorkTimeOnServer;

        this.queryWaitTimeOnClient = (double) queryWaitTimeOnClient.stream().mapToLong(i -> i).sum() / (double) queryWaitTimeOnClient.size();
    }

    @Override
    public String toString() {
        return "Result{" +
                "dataLength=" + dataLength +
                ", clientNumber=" + clientNumber +
                ", queryWaitTime=" + queryWaitTime +
                ", queryNumberPerClient=" + queryNumberPerClient +
                ", queryWorkTimeOnServer=" + queryWorkTimeOnServer +
                ", queryWaitTimeOnClient=" + queryWaitTimeOnClient +
                '}';
    }

    public int getDataLength() {
        return dataLength;
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public int getQueryWaitTime() {
        return queryWaitTime;
    }

    public int getQueryNumberPerClient() {
        return queryNumberPerClient;
    }

    public double getQueryWorkTimeOnServer() {
        return queryWorkTimeOnServer;
    }

    public double getQueryWaitTimeOnClient() {
        return queryWaitTimeOnClient;
    }
}
