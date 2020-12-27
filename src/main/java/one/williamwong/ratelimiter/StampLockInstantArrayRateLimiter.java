package one.williamwong.ratelimiter;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;

import static java.time.Duration.between;
import static java.time.Instant.now;

public class StampLockInstantArrayRateLimiter implements IRateLimiter {

    private final ISleeper sleeper;
    private final Duration duration;
    private final Instant[] records;
    private final StampedLock lock;
    private int pointer;

    public StampLockInstantArrayRateLimiter(final int maxInvokes, final Duration duration) {
        this(new Sleeper(), maxInvokes, duration);
    }

    public StampLockInstantArrayRateLimiter(final ISleeper sleeper, final int maxInvokes, final Duration duration) {
        if (duration.compareTo(Duration.ofMillis(1)) < 0) {
            throw new IllegalArgumentException("Cannot support rate limiter with duration less than 1ms");
        }
        this.sleeper = sleeper;
        this.duration = duration;
        this.records = new Instant[maxInvokes];
        this.lock = new StampedLock();
        this.pointer = 0;
    }

    @Override public void acquire() throws InterruptedException {
        final long stamp = lock.writeLock();
        try {
            Instant now = now();
            if (records[pointer] != null &&
                    between(records[pointer], now).compareTo(duration) < 0) {
                sleeper.sleepTill(records[pointer].plus(duration));
                now = now();
            }
            records[pointer] = now;
            pointer = (pointer + 1) % records.length;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override public void reset() {
        final long stamp = lock.writeLock();
        try {
            Arrays.fill(records, null);
            this.pointer = 0;
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
