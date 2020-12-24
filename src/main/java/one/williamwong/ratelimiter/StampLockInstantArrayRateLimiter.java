package one.williamwong.ratelimiter;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;

public class StampLockInstantArrayRateLimiter implements IRateLimiter {

    private final Duration duration;
    private final Instant[] records;
    private final StampedLock lock;
    private int pointer;

    public StampLockInstantArrayRateLimiter(int maxInvokes, Duration duration) {
        this.duration = duration;
        this.records = new Instant[maxInvokes];
        this.lock = new StampedLock();
        this.pointer = 0;
    }

    @Override public void acquire() {
        final long stamp = lock.writeLock();
        try {
            final Instant now = Instant.now();
            if (records[pointer] != null) {
                final Duration awayFromHead = Duration.between(records[pointer], now);
                if (awayFromHead.compareTo(duration) < 0) {
                    handleExcessLimit(records.length, awayFromHead);
                }
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
