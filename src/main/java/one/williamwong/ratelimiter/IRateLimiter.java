package one.williamwong.ratelimiter;

public interface IRateLimiter {
    void acquire() throws InterruptedException;

    void reset();

}
