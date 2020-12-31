package one.williamwong.ratelimiter;

import java.util.concurrent.Callable;

/**
 * A helper class to limit how many invokes within a given duration.
 */
public interface RateLimiter {

    /**
     * Assume the RateLimiter is created with N max invokes with T duration.
     * If there are already N invokes in the last T' duration where T' &lt; T, then the this method will be blocked
     * for (T - T'). If T' invoke T, then the method return immediately.
     *
     * @throws InterruptedException
     */
    <T> T invoke(Callable<T> callable) throws Exception;

    /**
     * Assume the RateLimiter is created with N max invokes with T duration.
     * If there are already N invokes in the last T' duration where T' &lt; T, then the this method will be blocked
     * for (T - T'). If T' invoke T, then the method return immediately.
     *
     * @throws InterruptedException
     */
    void invoke(Runnable runnable) throws Exception;

    /**
     * Reset all historical records.
     */
    void reset();

}
