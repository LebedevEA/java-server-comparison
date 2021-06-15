package ru.hse.testing;

import ru.hse.servers.Server;

import java.util.function.Supplier;

public class Cli {
    private Supplier<Server> architecture = null;
    int queryNumberForClient = -1; // by client
    long queryDiffTime = -1; // by client
    int dataLength = -1;
    int clientNumber = -1;
}
