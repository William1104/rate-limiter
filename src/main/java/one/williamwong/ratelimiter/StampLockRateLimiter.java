package one.williamwong.ratelimiter;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;

public class StampLockRateLimiter implements IRateLimiter {

    private final Duration duration;
    private final Instant[] records;
    private final StampedLock lock;
    private int head;
    private int tail;

    public StampLockRateLimiter(int maxInvokes, Duration duration) {
        this.duration = duration;
        this.records = new Instant[maxInvokes];
        this.lock = new StampedLock();
        this.head = 0;
        this.tail = records.length;
    }

    @Override
    public void acquire() {
        final Instant now = Instant.now();
        final long stamp = lock.writeLock();
        try {
            if (tail == head) {
                final Duration awayFromHead = Duration.between(records[head], now);
                if (awayFromHead.compareTo(duration) < 0) {
                    throw new RateLimitExcessException("excess rate limit");
                }
                records[tail % records.length] = now;
                head = newPointer(head);
                tail = newPointer(tail);
            } else {
                records[tail % records.length] = now;
                tail = newPointer(tail);
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override public void reset() {
        final long stamp = lock.writeLock();
        try {
            Arrays.fill(records, null);
            this.head = 0;
            this.tail = records.length;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    int newPointer(int currentPointer) {
        return (currentPointer + 1) % records.length;
    }

    public static class RateLimitExcessException extends RuntimeException {
        public RateLimitExcessException(String message) {
            super(message);
        }
    }
}
