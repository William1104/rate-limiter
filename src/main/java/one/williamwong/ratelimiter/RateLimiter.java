package one.williamwong.ratelimiter;

import java.util.concurrent.Callable;

/**
 * A helper class to limit how many invokes within a given duration.
 */
public interface RateLimiter {

    /**
     * Assume the RateLimiter is created with N max invokes with T duration.
     * If there are already N invokes in the last T' duration where T' &lt; T,
     * then the this method will be blocked for (T - T').
     * If T' invoke T, then the method return immediately.
     *
     * @return the release time (measured with System.nanoTime())
     * @throws InterruptedException if interrupted when the invocation is paused.
     */
    long invoke() throws InterruptedException;

    /**
     * Reset all historical records.
     */
    void reset();

}
