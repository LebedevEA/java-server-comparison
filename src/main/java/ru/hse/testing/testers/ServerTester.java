package ru.hse.testing.testers;

import ru.hse.servers.Server;
import ru.hse.testing.stats.ServerResult;

public class ServerTester {
    private final Server architecture;
    private final int dataLength; // aka N
    private final int clientNumber; // aka M
    private final int queryWaitTime; // aka ∆
    private final int queryNumberPerClient; // aka X

    public ServerTester(
            Server architecture,
            int dataLength,
            int clientNumber,
            int queryWaitTime,
            int queryNumberPerClient
    ) {
        this.architecture = architecture;
        this.dataLength = dataLength;
        this.clientNumber = clientNumber;
        this.queryWaitTime = queryWaitTime;
        this.queryNumberPerClient = queryNumberPerClient;
    }

    public ServerResult test() {
        // TODO;
        return null;
    }
}
