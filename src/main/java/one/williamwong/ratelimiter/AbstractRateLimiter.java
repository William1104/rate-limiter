package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.Arrays;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

abstract class AbstractRateLimiter implements RateLimiter {

    private final int maxInvokes;
    private final int density;
    private final long duration;
    private final long[] records;
    private int pointer;

    AbstractRateLimiter(int maxInvokes, Duration duration) {
        this(maxInvokes, duration, 1);
    }

    AbstractRateLimiter(int maxInvokes, Duration duration, int density) {
        if (density >= maxInvokes) {
            throw new IllegalArgumentException("Density cannot be more than max invokes");
        }
        this.maxInvokes = maxInvokes;
        this.density = density;
        this.duration = duration.toNanos();
        this.records = new long[(int) Math.ceil(maxInvokes * 1.0d / density)];
        this.pointer = 0;
    }

    protected void resetHistory() {
        Arrays.fill(records, 0);
        this.pointer = 0;
    }


    protected long pauseIfRequired() throws InterruptedException {
        long now = nanoTime();
        long referenceRecord = referenceRecord();
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

    protected long referenceRecord() {
        int recordPoint = pointer / density;
        long currRecord = records[recordPoint];
        long nextRecord = records[(recordPoint + 1) % records.length];
        if (currRecord == 0 || nextRecord == 0) {
            return 0;
        }
        return currRecord + (nextRecord - currRecord) / density;
    }

    protected long record(long now) {
        if (pointer % density == 0) {
            records[pointer / density] = now;
        }
        pointer = (pointer + 1) % maxInvokes;
        return now;
    }

}
