package one.williamwong.ratelimiter;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.StampedLock;

import static java.time.Duration.between;
import static java.time.Instant.now;

public class StampLockInstantArrayRateLimiter implements RateLimiter {

    private final Pauser pauser;
    private final Duration duration;
    private final Instant[] records;
    private final StampedLock lock;
    private int pointer;

    public StampLockInstantArrayRateLimiter(final int maxInvokes, final Duration duration) {
        this.pauser = Pauser.INSTANCE;
        this.duration = duration;
        this.records = new Instant[maxInvokes];
        this.lock = new StampedLock();
        this.pointer = 0;
    }

    @Override public <T> T invoke(Callable<T> callable) throws Exception {
        final long stamp = lock.writeLock();
        try {
            pauseIfRequired();
            return callable.call();
        } finally {
            record(now());
            lock.unlockWrite(stamp);
        }
    }

    @Override public void invoke(Runnable runnable) throws Exception {
        final long stamp = lock.writeLock();
        try {
            pauseIfRequired();
            runnable.run();
        } finally {
            record(now());
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

    private void pauseIfRequired() throws InterruptedException {
        Instant now = now();
        if (records[pointer] != null &&
                between(records[pointer], now).compareTo(duration) < 0) {
            pauser.pauseUntil(records[pointer].plus(duration));
        }
    }

    private void record(Instant now) {
        records[pointer] = now;
        pointer = (pointer + 1) % records.length;
    }
}
