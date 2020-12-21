package one.williamwong.ratelimiter;

public interface IRateLimiter {
    void acquire();
    void reset();
}
