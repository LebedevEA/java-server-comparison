package ru.hse.servers.nonblocking;

import ru.hse.servers.Server;
import ru.hse.utils.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

public class NonBlockingServer implements Server {
    private ServerSocketChannel serverSocketChannel = null;
    private RequestHandler requestHandler = null;
    private ResponseHandler responseHandler = null;

    private final ExecutorService serverService = Executors.newSingleThreadExecutor();
    private final ExecutorService requestService = Executors.newSingleThreadExecutor();
    private final ExecutorService responseService = Executors.newSingleThreadExecutor();
    private final ExecutorService workerThreadPool = Executors.newFixedThreadPool(16);

    private final Queue<NonBlockingClientHandler> addToRequests = new ConcurrentLinkedQueue<>();
    private final Queue<NonBlockingClientHandler> addToResponses = new ConcurrentLinkedQueue<>();

    private final Set<NonBlockingClientHandler> clients = ConcurrentHashMap.newKeySet();

    private volatile boolean isWorking = true;

    @Override
    public void start() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(Constants.PORT));

        serverService.submit(this::acceptClients);

        requestHandler = new RequestHandler(addToRequests);
        requestService.submit(requestHandler::run);

        responseHandler = new ResponseHandler(addToResponses);
        responseService.submit(responseHandler::run);
    }

    private void acceptClients() {
        try (ServerSocketChannel ignored = serverSocketChannel) {
            while (!Thread.interrupted() && isWorking) {
                try {
                    SocketChannel clientSocketChannel = serverSocketChannel.accept();
                    NonBlockingClientHandler client = new NonBlockingClientHandler(clientSocketChannel, workerThreadPool, this::registerResponse);
                    clients.add(client);
                    addToRequests.add(client);
                    requestHandler.wakeup();
                } catch (IOException ignored1) { }
            }
        } catch (IOException ignored) { }
    }

    private void registerResponse(NonBlockingClientHandler client) {
        addToResponses.add(client);
        responseHandler.wakeup();
    }

    @Override
    public void stop() throws IOException {
        isWorking = false;

        serverSocketChannel.close();
        serverService.shutdown();

        requestHandler.close();
        requestService.shutdown();

        workerThreadPool.shutdown();

        responseHandler.close();
        responseService.shutdown();

        clients.forEach(NonBlockingClientHandler::stop);
    }
}
