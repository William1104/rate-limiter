package one.williamwong.ratelimiter;

import java.time.Instant;

import static java.lang.System.nanoTime;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

class SimplePauser implements Pauser {

    @Override
    public void pauseUntil(final long until) throws InterruptedException {
        // Actual pause time  can be less than requested time.
        long pausedTime;
        while ((pausedTime = until - nanoTime()) > 0) {
            NANOSECONDS.sleep(pausedTime);
        }
    }

}
