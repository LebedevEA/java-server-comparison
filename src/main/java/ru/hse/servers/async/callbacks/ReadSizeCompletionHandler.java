package ru.hse.servers.async.callbacks;

import ru.hse.servers.async.Client;

import java.nio.channels.CompletionHandler;

public class ReadSizeCompletionHandler implements CompletionHandler<Integer, Client> {
    @Override
    public void completed(Integer result, Client attachment) {
        if (!attachment.isWorking() || result == -1) return;

        if (attachment.getRequests().hasRemaining()) {
            attachment.getSocketChannel().read(attachment.getRequests(), attachment, this);
            return;
        }
        attachment.getRequests().flip();
        int msgSize = attachment.getRequests().getInt();
        attachment.readMsg(msgSize);
    }

    @Override
    public void failed(Throwable exc, Client attachment) {
        throw new RuntimeException(exc);
    }
}
