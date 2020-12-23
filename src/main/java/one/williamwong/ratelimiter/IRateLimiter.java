package one.williamwong.ratelimiter;

import java.time.Duration;

public interface IRateLimiter {
    void acquire();

    void reset();

    default void handleExcessLimit(int count, Duration duration) {
        throw new RateExcessException("excess rate limit. got " + count + " invocations in " + duration.toNanos() + "ns");
    }

    class RateExcessException extends RuntimeException {
        public RateExcessException(String message) {
            super(message);
        }
    }

}
