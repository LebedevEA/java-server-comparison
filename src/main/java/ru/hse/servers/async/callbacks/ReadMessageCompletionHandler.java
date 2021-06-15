package ru.hse.servers.async.callbacks;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.hse.servers.async.Client;
import ru.hse.utils.Utils;

import java.nio.channels.CompletionHandler;

import static ru.hse.utils.Utils.bubbleSort;
import static ru.hse.utils.Utils.serializeArray;

public class ReadMessageCompletionHandler implements CompletionHandler<Integer, Client> {
    @Override
    public void completed(Integer result, Client attachment) {
        if (!attachment.isWorking() || result == -1) return;

        if (attachment.getRequests().hasRemaining()) {
            attachment.getSocketChannel().read(attachment.getRequests(), attachment, this);
        }
        try {
            int[] data = Utils.readArray(attachment.getRequests().array());
            attachment.getWorkerThreadPool().submit(() -> {
                bubbleSort(data);
                attachment.writeData(serializeArray(data));
            });
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        attachment.run();
    }

    @Override
    public void failed(Throwable exc, Client attachment) {
        throw new RuntimeException(exc);
    }
}
