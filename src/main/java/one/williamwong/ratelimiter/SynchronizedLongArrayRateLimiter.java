package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.Arrays;

import static java.lang.System.nanoTime;
import static java.time.Instant.now;

public class SynchronizedLongArrayRateLimiter implements IRateLimiter {

    private final ISleeper sleeper;
    private final long duration;
    private final long[] records;
    private final Object lock;
    private int pointer;

    public SynchronizedLongArrayRateLimiter(final int maxInvokes, final Duration duration) {
        this(new Sleeper(), maxInvokes, duration);
    }

    public SynchronizedLongArrayRateLimiter(final ISleeper sleeper, final int maxInvokes, final Duration duration) {
        this.sleeper = sleeper;
        this.duration = duration.toNanos();
        this.records = new long[maxInvokes];
        this.lock = new Object();
        this.pointer = 0;
    }

    @Override
    public void acquire() throws InterruptedException {
        synchronized (lock) {
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
        }
    }

    @Override public void reset() {
        synchronized (lock) {
            Arrays.fill(records, 0);
            this.pointer = 0;
        }
    }

}
