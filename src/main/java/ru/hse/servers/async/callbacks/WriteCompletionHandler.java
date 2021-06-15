package ru.hse.servers.async.callbacks;

import ru.hse.servers.async.WriteWrapper;

import java.nio.channels.CompletionHandler;

public class WriteCompletionHandler implements CompletionHandler<Integer, WriteWrapper> {
    @Override
    public void completed(Integer result, WriteWrapper attachment) {
        if (!attachment.isWorking() || result == -1) return;

        if (attachment.getBuffer().hasRemaining()) {
            attachment.getSocketChannel().write(attachment.getBuffer(), attachment, this);
        }
    }

    @Override
    public void failed(Throwable ignored, WriteWrapper attachment) { }
}
