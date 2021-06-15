package ru.hse.testing.stats;

import java.util.List;

public class ServerResult {
    private final double queryWorkTimeOnServer;
    private final List<Integer> queryWaitTimeOnClient;

    public ServerResult(double queryWorkTimeOnServer, List<Integer> queryWaitTimeOnClient) {
        this.queryWorkTimeOnServer = queryWorkTimeOnServer;
        this.queryWaitTimeOnClient = queryWaitTimeOnClient;
    }

    public double getQueryWorkTimeOnServer() {
        return queryWorkTimeOnServer;
    }

    public List<Integer> getQueryWaitTimeOnClient() {
        return queryWaitTimeOnClient;
    }
}
