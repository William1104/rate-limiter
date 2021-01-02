package one.williamwong.ratelimiter;

import java.time.Instant;

/**
 * A Pauser pause the execution until a given time.
 */
interface Pauser {

    Pauser INSTANCE = new SimplePauser();

    /**
     * Pause the execution until the given time
     *
     * @param until the execution is paused up to 'until' time. This 'until' time is measured with System.nanoTime().
     * @throws InterruptedException
     */
    void pauseUntil(long until) throws InterruptedException;
}
