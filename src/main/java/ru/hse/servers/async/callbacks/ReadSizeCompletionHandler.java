package ru.hse.servers.async.callbacks;

import ru.hse.servers.async.AsyncClientHandler;

import java.nio.channels.CompletionHandler;

public class ReadSizeCompletionHandler implements CompletionHandler<Integer, AsyncClientHandler> {
    @Override
    public void completed(Integer result, AsyncClientHandler attachment) {
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
    public void failed(Throwable exc, AsyncClientHandler attachment) {
        throw new RuntimeException(exc);
    }
}
