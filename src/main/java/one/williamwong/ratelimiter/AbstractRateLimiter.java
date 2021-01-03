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

    /**
     * Create a rate limiter which allows at most 'maxInvokes' invocations in last 'duration'.
     * <p>
     * This rate limiter records the history of last 'maxInvokes' invocations, so that it knows whether it should
     * block current execution. The higher maxInvokes value, the more memory will be needed. To reduce the amount
     * of memory required, we can adjust the `density` value. If `density` is 1, this rate limiter records the
     * invocation time of every last 'maxInvokes' invocations. If `density` is N, this rate limiter records the
     * invocation time of 1st, N+1th, 2N+1th, .. up to maxInvokes th invocations. The higher density value, less memory
     * will be required. However, a higher density value also means less accurate this rate limiter will be.
     *
     * @param maxInvokes
     * @param duration
     * @param density
     */
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
        if (currRecord == 0 || nextRecord == 0 || nextRecord < currRecord) {
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
