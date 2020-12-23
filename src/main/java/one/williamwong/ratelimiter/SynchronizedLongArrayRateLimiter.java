package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.Arrays;

public class SynchronizedLongArrayRateLimiter implements IRateLimiter {

    private final long duration;
    private final long[] records;
    private final Object lock;
    private int pointer;

    public SynchronizedLongArrayRateLimiter(int maxInvokes, Duration duration) {
        this.duration = duration.toNanos();
        this.records = new long[maxInvokes];
        this.lock = new Object();
        this.pointer = 0;
    }

    @Override
    public void acquire() {
        final long now = System.nanoTime();
        synchronized (lock) {
            if (records[pointer] != 0) {
                final long awayFromHead = now - records[pointer];
                if (awayFromHead < duration) {
                    handleExcessLimit(records.length, Duration.ofNanos(awayFromHead));
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
