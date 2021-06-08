package ru.hse.servers;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ServerTest {
    private void baseServerTest(Supplier<Server> makeServer) throws Exception {
        int clientNum = 100;

        Server server = makeServer.get();
        server.start();

        ExecutorService clientsPool = Executors.newCachedThreadPool();
        var futures = clientsPool.invokeAll(
                IntStream.range(0, clientNum).mapToObj(i -> new TestClient()).collect(Collectors.toList())
        );
        List<ArrayTestPair> pairs = new ArrayList<>();
        for (Future<ArrayTestPair> future : futures) {
            pairs.add(future.get());
        }

        server.stop();

        pairs.forEach(ArrayTestPair::check);
    }

    @Test
    public void basicTest() throws Exception {
        baseServerTest(BasicServer::new);
    }
}