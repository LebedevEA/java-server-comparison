package ru.hse.servers.async.callbacks;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.hse.servers.async.AsyncClientHandler;
import ru.hse.utils.ArrayHolder;
import ru.hse.utils.Utils;

import java.nio.channels.CompletionHandler;

import static ru.hse.utils.Utils.bubbleSort;
import static ru.hse.utils.Utils.serializeArray;

public class ReadMessageCompletionHandler implements CompletionHandler<Integer, AsyncClientHandler> {
    @Override
    public void completed(Integer result, AsyncClientHandler attachment) {
        if (!attachment.isWorking() || result == -1) return;

        if (attachment.getRequests().hasRemaining()) {
            attachment.getSocketChannel().read(attachment.getRequests(), attachment, this);
        }
        try {
            ArrayHolder data = Utils.readArray(attachment.getRequests().array());
            attachment.getWorkerThreadPool().submit(() -> {
                bubbleSort(data.getArray());
                attachment.writeData(serializeArray(data.getArray(), data.getId()));
            });
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exc, AsyncClientHandler attachment) {
        throw new RuntimeException(exc);
    }
}
