package one.williamwong.ratelimiter;

import java.time.Instant;

public interface ISleeper {

    void sleepTill(final Instant endTime) throws InterruptedException;
}
