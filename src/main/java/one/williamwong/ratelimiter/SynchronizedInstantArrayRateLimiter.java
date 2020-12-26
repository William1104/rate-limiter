package one.williamwong.ratelimiter;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static java.time.Duration.between;
import static java.time.Instant.now;

public class SynchronizedInstantArrayRateLimiter implements IRateLimiter {

    private final ISleeper sleeper;
    private final Duration duration;
    private final Instant[] records;
    private final Object lock;
    private int pointer;

    public SynchronizedInstantArrayRateLimiter(final ISleeper sleeper, int maxInvokes, Duration duration) {
        if (duration.compareTo(Duration.ofMillis(1)) < 0) {
            throw new IllegalArgumentException("Cannot support rate limiter with duration less than 1ms");
        }
        this.sleeper = sleeper;
        this.duration = duration;
        this.records = new Instant[maxInvokes];
        this.lock = new Object();
        this.pointer = 0;
    }

    @Override
    public void acquire() throws InterruptedException {
        synchronized (lock) {
            Instant now = now();
            if (records[pointer] != null &&
                    between(records[pointer], now).compareTo(duration) < 0) {
                sleeper.sleepTill(records[pointer].plus(duration));
                now = now();
            }
            records[pointer] = now;
            pointer = (pointer + 1) % records.length;
        }
    }

    @Override public void reset() {
        synchronized (lock) {
            Arrays.fill(records, null);
            this.pointer = 0;
        }
    }

}
