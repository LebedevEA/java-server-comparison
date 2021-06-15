package ru.hse.testing.stats;

import java.util.List;

public class ServerResult {
    private final List<Integer> queryWorkTimeOnServer;
    private final List<Integer> queryWaitTimeOnClient;

    public ServerResult(List<Integer> queryWorkTimeOnServer, List<Integer> queryWaitTimeOnClient) {
        this.queryWorkTimeOnServer = queryWorkTimeOnServer;
        this.queryWaitTimeOnClient = queryWaitTimeOnClient;
    }

    public List<Integer> getQueryWorkTimeOnServer() {
        return queryWorkTimeOnServer;
    }

    public List<Integer> getQueryWaitTimeOnClient() {
        return queryWaitTimeOnClient;
    }
}
