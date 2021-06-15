package ru.hse.testing.app;

import ru.hse.servers.async.AsyncServer;
import ru.hse.servers.blocking.BlockingServer;
import ru.hse.servers.nonblocking.NonBlockingServer;
import ru.hse.testing.stats.Parameter;
import ru.hse.testing.stats.TestingState;

import java.util.Scanner;

public class Cli {
    private final TestingState state = TestingState.empty();

    /**
     *
     * @return true if collected, false if stopped
     */
    public boolean collectData() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Write \"exit\" to exit");
        if (!scanArchitecture(scanner) || !scanQueryNumberPerClient(scanner) || !scanChangingParameter(scanner)) {
            return false;
        }

        boolean set = switch (state.getChanging()) {
            case DATA_LENGTH -> scanClientNumber(scanner) && scanQueryWaitTime(scanner);
            case CLIENT_NUMBER -> scanDataLength(scanner) && scanQueryWaitTime(scanner);
            case QUERY_WAIT_TIME -> scanDataLength(scanner) && scanClientNumber(scanner);
        };

        if (!set) {
            return false;
        }

        return scanStep(scanner) && scanLower(scanner) && scanUpper(scanner);
    }

    private boolean scanStep(Scanner scanner) {
        System.out.println("Input step for changing parameter");
        while (true) {
            System.out.print(">> ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("exit")) return false;

            if (!input.matches("-?\\d+")) {
                System.out.println("Could not parse integer, try again");
                continue;
            }

            state.setStep(Integer.parseInt(input));

            if (state.getStep() >= 1) {
                break;
            } else {
                System.out.println("Are you sure about " + state.getStep() + "? Try again...");
            }
        }
        return true;
    }

    private boolean scanLower(Scanner scanner) {
        System.out.println("Input lower limit for changing parameter");
        while (true) {
            System.out.print(">> ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("exit")) return false;

            if (!input.matches("-?\\d+")) {
                System.out.println("Could not parse integer, try again");
                continue;
            }

            state.setLowerLimit(Integer.parseInt(input));

            if (state.getLowerLimit() >= 1) {
                break;
            } else {
                System.out.println("Are you sure about " + state.getLowerLimit() + "? Try again...");
            }
        }
        return true;
    }

    private boolean scanUpper(Scanner scanner) {
        System.out.println("Input upper limit for changing parameter");
        while (true) {
            System.out.print(">> ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("exit")) return false;

            if (!input.matches("-?\\d+")) {
                System.out.println("Could not parse integer, try again");
                continue;
            }

            state.setUpperBound(Integer.parseInt(input));

            if (state.getUpperBound() >= 1) {
                break;
            } else {
                System.out.println("Are you sure about " + state.getUpperBound() + "? Try again...");
            }
        }
        return true;
    }

    private boolean scanArchitecture(Scanner scanner) {
        System.out.println("Input testing architecture [blocking, nonblocking, asynchronous]");
        while (true) {
            System.out.print(">> ");
            String input = scanner.nextLine();
            input = input.trim().toLowerCase();
            if ("blocking".equals(input)) {
                state.setArchitecture(BlockingServer::new);
            } else if ("nonblocking".equals(input)) {
                state.setArchitecture(NonBlockingServer::new);
            } else if ("asynchronous".equals(input)) {
                state.setArchitecture(AsyncServer::new);
            } else if ("exit".equals(input)) {
                return false;
            }

            if (state.getArchitecture() != null) {
                break;
            } else {
                System.out.println("Could not parse, try again");
            }
        }
        return true;
    }

    private boolean scanQueryNumberPerClient(Scanner scanner) {
        System.out.println("Input number of queries for one client");
        while (true) {
            System.out.print(">> ");
            String input = scanner.nextLine();
            input = input.trim().toLowerCase();
            if (input.equals("exit")) return false;

            if (!input.matches("-?\\d+")) {
                System.out.println("Could not parse integer, try again");
                continue;
            }

            state.setQueryNumberPerClient(Integer.parseInt(input));

            if (state.getQueryNumberPerClient() >= 1) {
                break;
            } else {
                System.out.println("Are you sure about " + state.getQueryNumberPerClient()
                        + " queries per client? Try again...");
            }
        }
        return true;
    }

    private boolean scanChangingParameter(Scanner scanner) {
        System.out.println("Input changing parameter (as 1, 2, or 3 as follows)");
        System.out.println("  1 -> Sorting array length");
        System.out.println("  2 -> Number of clients");
        System.out.println("  3 -> Wait time between queries");

        while (true) {
            System.out.print(">> ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("exit")) return false;

            if (!input.matches("\\d")) {
                System.out.println("Could not parse digit, try again");
                continue;
            }

            switch (Integer.parseInt(input)) {
                case 1 -> state.setChanging(Parameter.DATA_LENGTH);
                case 2 -> state.setChanging(Parameter.CLIENT_NUMBER);
                case 3 -> state.setChanging(Parameter.QUERY_WAIT_TIME);
                default -> {
                    System.out.println("Could not parse [1-3], try again");
                    continue;
                }
            }
            return true;
        }
    }

    private boolean scanDataLength(Scanner scanner) {
        System.out.println("Input length of sorting array");
        while (true) {
            System.out.print(">> ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("exit")) return false;

            if (!input.matches("-?\\d+")) {
                System.out.println("Could not parse integer, try again");
                continue;
            }

            state.setDataLength(Integer.parseInt(input));

            if (state.getDataLength() >= 1) {
                break;
            } else {
                System.out.println("Are you sure about length of " + state.getDataLength()
                        + "? Try again...");
            }
        }
        return true;
    }

    private boolean scanClientNumber(Scanner scanner) {
        System.out.println("Input number of clients");
        while (true) {
            System.out.print(">> ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("exit")) return false;

            if (!input.matches("-?\\d+")) {
                System.out.println("Could not parse integer, try again");
                continue;
            }

            state.setClientNumber(Integer.parseInt(input));

            if (state.getClientNumber() >= 1) {
                break;
            } else {
                System.out.println("Are you sure about " + state.getClientNumber()
                        + " clients? Try again...");
            }
        }
        return true;
    }

    private boolean scanQueryWaitTime(Scanner scanner) {
        System.out.println("Input wait time between queries (in ms)");
        while (true) {
            System.out.print(">> ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("exit")) return false;

            if (!input.matches("-?\\d+")) {
                System.out.println("Could not parse integer, try again");
                continue;
            }

            state.setQueryWaitTime(Integer.parseInt(input));

            if (state.getQueryWaitTime() >= 1) {
                break;
            } else {
                System.out.println("Are you sure about " + state.getQueryWaitTime()
                        + " seconds? Try again...");
            }
        }
        return true;
    }

    public TestingState getState() {
        return state;
    }
}
