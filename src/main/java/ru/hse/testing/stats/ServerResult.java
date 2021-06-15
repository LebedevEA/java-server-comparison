package ru.hse.testing.stats;

import java.util.List;

public class ServerResult {
    private final int queryWorkTimeOnServer;
    private final List<Integer> queryWaitTimeOnClient;

    public ServerResult(int queryWorkTimeOnServer, List<Integer> queryWaitTimeOnClient) {
        this.queryWorkTimeOnServer = queryWorkTimeOnServer;
        this.queryWaitTimeOnClient = queryWaitTimeOnClient;
    }

    public int getQueryWorkTimeOnServer() {
        return queryWorkTimeOnServer;
    }

    public List<Integer> getQueryWaitTimeOnClient() {
        return queryWaitTimeOnClient;
    }
}
