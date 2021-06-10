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
    private void baseServerTest(Supplier<Server> makeServer, int clientNum) throws Exception {
        Server server = makeServer.get();
        server.start();

        ExecutorService clientsPool = Executors.newFixedThreadPool(64);
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
        baseServerTest(BasicServer::new, 10);
        baseServerTest(BasicServer::new, 100);
        baseServerTest(BasicServer::new, 1000);
        baseServerTest(BasicServer::new, 10000); // takes ~17 seconds to work, but work on my pc
    }

    @Test
    public void nonBlockingTest() throws Exception {
        baseServerTest(NonBlockingServer::new, 10);
    }
}