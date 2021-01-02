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
     * @param until
     * @throws InterruptedException
     */
    void pauseUntil(final Instant until) throws InterruptedException;

    void pauseUntil(long nano) throws InterruptedException;
}
