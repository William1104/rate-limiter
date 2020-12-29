package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;

import static java.lang.System.nanoTime;
import static java.time.Instant.now;

public class StampLockLongArrayRateLimiter implements IRateLimiter {

    private final ISleeper sleeper;
    private final long duration;
    private final long[] records;
    private final StampedLock lock;
    private int pointer;

    public StampLockLongArrayRateLimiter(int maxInvokes, Duration duration) {
        this(new Sleeper(), maxInvokes, duration);
    }

    public StampLockLongArrayRateLimiter(ISleeper sleeper, int maxInvokes, Duration duration) {
        this.sleeper = sleeper;
        this.duration = duration.toNanos();
        this.records = new long[maxInvokes];
        this.lock = new StampedLock();
        this.pointer = 0;
    }

    @Override public void acquire() throws InterruptedException {
        final long stamp = lock.writeLock();
        try {
            long now = nanoTime();
            if (records[pointer] != 0) {
                long awayFromLastRecord = now - records[pointer];
                if (awayFromLastRecord < duration) {
                    sleeper.sleepTill(now().plusNanos(duration - awayFromLastRecord));
                    now = nanoTime();
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
