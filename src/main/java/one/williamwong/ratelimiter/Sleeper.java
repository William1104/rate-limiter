package one.williamwong.ratelimiter;

import java.time.Instant;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class Sleeper implements ISleeper {

    @Override
    public void sleepTill(final Instant endTime) throws InterruptedException {
        long sleepTime;
        // Actual sleep time can be less than requested sleep time.
        while ((sleepTime = between(now(), endTime).toNanos()) > 0) {
            NANOSECONDS.sleep(sleepTime);
        }
    }
}
