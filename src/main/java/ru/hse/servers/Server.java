package ru.hse.servers;

import java.io.IOException;

public interface Server { // TODO make autocloseable
    void start() throws IOException;
    void stop() throws IOException;
}
