package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;

public class StampLockLongArrayRateLimiter implements IRateLimiter {

    private final long duration;
    private final long[] records;
    private final StampedLock lock;
    private int pointer;

    public StampLockLongArrayRateLimiter(int maxInvokes, Duration duration) {
        this.duration = duration.toNanos();
        this.records = new long[maxInvokes];
        this.lock = new StampedLock();
        this.pointer = 0;
    }

    @Override public void acquire() {
        final long now = System.nanoTime();
        final long stamp = lock.writeLock();
        try {
            if (records[pointer] != 0) {
                final long awayFromHead = now - records[pointer];
                if (awayFromHead < duration) {
                    handleExcessLimit(records.length, Duration.ofNanos(awayFromHead));
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
            Arrays.fill(records, 0);
            this.pointer = 0;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

}
