package one.williamwong.ratelimiter;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static java.time.Duration.between;
import static java.time.Instant.now;

public class SynchronizedInstantArrayRateLimiter implements RateLimiter {

    private final Pauser pauser;
    private final Duration duration;
    private final Instant[] records;
    private final Object lock;
    private int pointer;

    public SynchronizedInstantArrayRateLimiter(int maxInvokes, Duration duration) {
        this.pauser = Pauser.INSTANCE;
        this.duration = duration;
        this.records = new Instant[maxInvokes];
        this.lock = new Object();
        this.pointer = 0;
    }

    @Override
    public <T> T invoke(final Callable<T> callable) throws Exception {
        synchronized (lock) {
            try {
                pauseIfRequired();
                return callable.call();
            } finally {
                record(now());
            }
        }
    }

    @Override
    public void invoke(final Runnable runnable) throws Exception {
        synchronized (lock) {
            try {
                pauseIfRequired();
                runnable.run();
            } finally {
                record(now());
            }
        }
    }

    @Override public void reset() {
        synchronized (lock) {
            Arrays.fill(records, null);
            this.pointer = 0;
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
