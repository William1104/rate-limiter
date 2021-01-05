package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.Arrays;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

abstract class AbstractRateLimiter implements RateLimiter {

    private final int maxInvokes;
    private final int samplingInterval;
    private final long duration;
    private final long[] records;
    private int pointer;

    /**
     * Create a rate limiter which allows at most 'maxInvokes' invocations in last 'duration'.
     * <p>
     * This rate limiter records the history of last 'maxInvokes' invocations, so that it knows whether it should
     * block current execution. The higher maxInvokes value, the more memory will be needed. To reduce the amount
     * of memory required, we can adjust the `samplingInterval` value. If `samplingInterval` is 1, this rate limiter records the
     * invocation time of every last 'maxInvokes' invocations. If `samplingInterval` is N, this rate limiter records the
     * invocation time of 1st, N+1th, 2N+1th, .. up to maxInvokes th invocations. The higher samplingInterval value, less memory
     * will be required. However, a higher samplingInterval value also means less accurate this rate limiter will be.
     *
     * @param maxInvokes
     * @param duration
     * @param samplingInterval
     */
    AbstractRateLimiter(int maxInvokes, Duration duration, int samplingInterval) {
        if (samplingInterval >= maxInvokes) {
            throw new IllegalArgumentException("samplingInterval cannot be more than max invokes");
        }
        this.maxInvokes = maxInvokes;
        this.samplingInterval = samplingInterval;
        this.duration = duration.toNanos();
        this.records = new long[(int) Math.ceil(maxInvokes * 1.0d / samplingInterval)];
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
        int recordPoint = pointer / samplingInterval;
        long currRecord = records[recordPoint];
        long nextRecord = records[(recordPoint + 1) % records.length];
        if (currRecord == 0 || nextRecord == 0 || nextRecord < currRecord) {
            return 0;
        }
        return currRecord + (nextRecord - currRecord) / samplingInterval;
    }

    protected long record(long now) {
        if (pointer % samplingInterval == 0) {
            records[pointer / samplingInterval] = now;
        }
        pointer = (pointer + 1) % maxInvokes;
        return now;
    }

}
