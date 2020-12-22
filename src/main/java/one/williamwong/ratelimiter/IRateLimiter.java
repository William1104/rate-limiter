package one.williamwong.ratelimiter;

public interface IRateLimiter {
    void acquire();

    void reset();

    class RateExcessException extends RuntimeException {
        public RateExcessException(String message) {
            super(message);
        }
    }

}
