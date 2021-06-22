package ru.hse.testing.stats;

import ru.hse.servers.Server;
import ru.hse.testing.testers.ServerTester;

import java.util.function.Supplier;

public class TestingState {
    private Supplier<Server> architecture = null;
    private int dataLength = -1; // aka N
    private int clientNumber = -1; // aka M
    private int queryWaitTime = -1; // aka ∆
    private int queryNumberPerClient = -1; // aka X

    private Parameter changing = null;

    private int upperBound = -1;
    private int step = -1;

    private TestingState() { }

    public Supplier<Server> getArchitecture() {
        return architecture;
    }

    public int getDataLength() {
        return dataLength;
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public int getQueryWaitTime() {
        return queryWaitTime;
    }

    public int getQueryNumberPerClient() {
        return queryNumberPerClient;
    }

    public Parameter getChanging() {
        return changing;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public int getStep() {
        return step;
    }

    public void setArchitecture(Supplier<Server> architecture) {
        this.architecture = architecture;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public void setClientNumber(int clientNumber) {
        this.clientNumber = clientNumber;
    }

    public void setQueryWaitTime(int queryWaitTime) {
        this.queryWaitTime = queryWaitTime;
    }

    public void setQueryNumberPerClient(int queryNumberPerClient) {
        this.queryNumberPerClient = queryNumberPerClient;
    }

    public void setChanging(Parameter changing) {
        this.changing = changing;
    }

    public void setUpperBound(int upperBound) {
        this.upperBound = upperBound;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public static TestingState empty() {
        return new TestingState();
    }

    public void setLowerLimit(int value) {
        switch (changing) {
            case DATA_LENGTH:
                dataLength = value;
                break;
            case CLIENT_NUMBER:
                clientNumber = value;
                break;
            case QUERY_WAIT_TIME:
                queryWaitTime = value;
                break;
        }
    }

    public int getLowerLimit() {
        switch (changing) {
            case DATA_LENGTH:
                return dataLength;
            case CLIENT_NUMBER:
                return clientNumber;
            case QUERY_WAIT_TIME:
                return queryWaitTime;
            default:
                throw new IllegalArgumentException();
        }
    }

    public boolean isValid() {
        switch (changing) {
            case DATA_LENGTH:
                return dataLength <= upperBound;
            case CLIENT_NUMBER:
                return clientNumber <= upperBound;
            case QUERY_WAIT_TIME:
                return queryWaitTime <= upperBound;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void makeStep() {
        switch (changing) {
            case DATA_LENGTH:
                dataLength += step;
                break;
            case CLIENT_NUMBER:
                clientNumber += step;
                break;
            case QUERY_WAIT_TIME:
                queryWaitTime += step;
                break;
        }
    }

    public Result runTest() throws Exception {
        Server server = architecture.get();
        var tester = new ServerTester(server, dataLength, clientNumber, queryWaitTime, queryNumberPerClient);
        ServerResult result = tester.test();
        return new Result(this, result.getQueryWorkTimeOnServer(), result.getQueryWaitTimeOnClient());
    }
}
