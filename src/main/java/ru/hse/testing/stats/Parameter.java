package ru.hse.testing.stats;

public enum Parameter {
    DATA_LENGTH {
        @Override
        public String toString() {
            return "data-length";
        }
    },
    CLIENT_NUMBER {
        @Override
        public String toString() {
            return "client-number";
        }
    },
    QUERY_WAIT_TIME {
        @Override
        public String toString() {
            return "query-wait-time";
        }
    },
}
