package ru.hse.servers;

import org.junit.jupiter.api.Test;
import ru.hse.servers.async.AsyncServer;
import ru.hse.servers.basic.BasicServer;
import ru.hse.servers.nonblocking.NonBlockingServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
                IntStream.range(0, clientNum).mapToObj(i -> new TestClient(new Random(i))).collect(Collectors.toList())
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
        baseServerTest(BasicServer::new, 1);
        baseServerTest(BasicServer::new, 10);
        baseServerTest(BasicServer::new, 100);
        baseServerTest(BasicServer::new, 1000);
    }

    @Test
    public void nonBlockingTest() throws Exception {
        baseServerTest(NonBlockingServer::new, 1);
        baseServerTest(NonBlockingServer::new, 10);
        baseServerTest(NonBlockingServer::new, 100);
        baseServerTest(NonBlockingServer::new, 1000);
    }

    @Test
    public void asyncTest() throws Exception {
        baseServerTest(AsyncServer::new, 1);
        baseServerTest(AsyncServer::new, 10);
        baseServerTest(AsyncServer::new, 100);
        baseServerTest(AsyncServer::new, 1000);
    }
}