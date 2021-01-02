package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.StampedLock;

import static java.lang.System.nanoTime;

public class StampLockLongArrayRateLimiter implements RateLimiter {

    private final Pauser pauser;
    private final long duration;
    private final long[] records;
    private final StampedLock lock;
    private int pointer;

    public StampLockLongArrayRateLimiter(int maxInvokes, Duration duration) {
        this.pauser = Pauser.INSTANCE;
        this.duration = duration.toNanos();
        this.records = new long[maxInvokes];
        this.lock = new StampedLock();
        this.pointer = 0;
    }

    @Override public void invoke() throws Exception {
        final long stamp = lock.writeLock();
        try {
            record(pauseIfRequired());
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

    private long pauseIfRequired() throws InterruptedException {
        long now = nanoTime();
        long referenceRecord = records[pointer];
        if (referenceRecord != 0 && (now - referenceRecord) < duration) {
            long until = duration + referenceRecord;
            pauser.pauseUntil(until);
            return until;
        }
        return now;
    }

    private void record(long now) {
        records[pointer] = now;
        pointer = (pointer + 1) % records.length;
    }
}
