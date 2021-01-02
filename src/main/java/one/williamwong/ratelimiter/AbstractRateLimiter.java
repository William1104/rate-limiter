package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.Arrays;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

abstract class AbstractRateLimiter implements RateLimiter {

    private final long duration;
    private final long[] records;
    private int pointer;

    public AbstractRateLimiter(int maxInvokes, Duration duration) {
        this.duration = duration.toNanos();
        this.records = new long[maxInvokes];
        this.pointer = 0;
    }

    protected void resetHistory() {
        Arrays.fill(records, 0);
        this.pointer = 0;
    }


    protected long pauseIfRequired() throws InterruptedException {
        long now = nanoTime();
        long referenceRecord = records[pointer];
        if (referenceRecord != 0 && (now - referenceRecord) < duration) {
            long until = duration + referenceRecord;
            long pausedTime;
            while ((pausedTime = until - nanoTime()) > 0) {
                NANOSECONDS.sleep(pausedTime);
            }
            return until;
        }
        return now;
    }

    protected long record(long now) {
        records[pointer] = now;
        pointer = (pointer + 1) % records.length;
        return now;
    }

}
