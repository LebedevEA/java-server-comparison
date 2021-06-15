package ru.hse.testing.testers;

import ru.hse.servers.Server;
import ru.hse.testing.stats.ServerResult;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerTester {
    private final Server server;
    private final int dataLength; // aka N
    private final int clientNumber; // aka M
    private final int queryWaitTime; // aka ∆
    private final int queryNumberPerClient; // aka X

    private final AtomicBoolean isAnyDone = new AtomicBoolean(false);

    public ServerTester(
            Server server,
            int dataLength,
            int clientNumber,
            int queryWaitTime,
            int queryNumberPerClient
    ) {
        this.server = server;
        this.dataLength = dataLength;
        this.clientNumber = clientNumber;
        this.queryWaitTime = queryWaitTime;
        this.queryNumberPerClient = queryNumberPerClient;
    }

    public ServerResult test() throws Exception {
        server.start();

        server.setIsAnyDone(isAnyDone);

        ExecutorService clientsPool = Executors.newFixedThreadPool(clientNumber);
        var clients = IntStream
                        .range(0, clientNumber)
                        .mapToObj(i ->
                                new Client(new Random(i), dataLength, queryWaitTime, queryNumberPerClient, isAnyDone)
                        )
                        .collect(Collectors.toList());
        var futures = clientsPool.invokeAll(clients);
        for (Future<Void> future : futures) {
            future.get();
        }

        clientsPool.shutdown();
        server.stop();

        List<Integer> queryWaitTimeOnClient = clients
                .stream()
                .flatMap(client -> client.getWaitTimes().stream())
                .collect(Collectors.toList());

        int queryWorkTimeOnServer = server.getServerWorkTime();
        return new ServerResult(queryWorkTimeOnServer, queryWaitTimeOnClient);
    }
}
