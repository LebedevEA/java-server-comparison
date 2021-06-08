package ru.hse.servers;

import java.io.IOException;

public interface Server {
    void start() throws IOException;
    void stop() throws IOException;
}
