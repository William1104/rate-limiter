package one.williamwong.ratelimiter;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

public class SynchronizedInstantArrayRateLimiter implements IRateLimiter {

    private final Duration duration;
    private final Instant[] records;
    private final Object lock;
    private int pointer;

    public SynchronizedInstantArrayRateLimiter(int maxInvokes, Duration duration) {
        this.duration = duration;
        this.records = new Instant[maxInvokes];
        this.lock = new Object();
        this.pointer = 0;
    }

    @Override
    public void acquire() {
        final Instant now = Instant.now();
        synchronized (lock) {
            if (records[pointer] != null) {
                final Duration awayFromHead = Duration.between(records[pointer], now);
                if (awayFromHead.compareTo(duration) < 0) {
                    handleExcessLimit(records.length, awayFromHead);
                }
            }
            records[pointer] = now;
            pointer = (pointer + 1) % records.length;
        }
    }

    @Override public void reset() {
        synchronized (lock) {
            Arrays.fill(records, null);
            this.pointer = 0;
        }
    }

}
