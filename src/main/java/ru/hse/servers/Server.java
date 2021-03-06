package ru.hse.servers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Server {
    protected AtomicBoolean isAnyDone = new AtomicBoolean(false);
    protected AtomicInteger workTime = new AtomicInteger(0);
    protected AtomicInteger workCounter = new AtomicInteger(0);

    public abstract void start() throws IOException;
    public abstract void stop() throws IOException;

    public void setIsAnyDone(AtomicBoolean isAnyDone) {
        this.isAnyDone = isAnyDone;
    }

    public double getServerWorkTime() {
        double sum = workTime.get();
        double num = workCounter.get();
        return sum / num;
    }

    protected void addWorkTime(int time) {
        if (!isAnyDone.get()) {
            workTime.addAndGet(time);
            workCounter.incrementAndGet();
        }
    }
}
