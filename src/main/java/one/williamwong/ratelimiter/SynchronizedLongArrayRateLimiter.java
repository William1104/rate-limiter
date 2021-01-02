package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static java.lang.System.nanoTime;

public class SynchronizedLongArrayRateLimiter implements RateLimiter {

    private final Pauser pauser;
    private final long duration;
    private final long[] records;
    private final Object lock;
    private int pointer;

    public SynchronizedLongArrayRateLimiter(final int maxInvokes, final Duration duration) {
        this.pauser = Pauser.INSTANCE;
        this.duration = duration.toNanos();
        this.records = new long[maxInvokes];
        this.lock = new Object();
        this.pointer = 0;
    }

    @Override
    public void invoke() throws Exception {
        synchronized (lock) {
            record(pauseIfRequired());
        }
    }

    @Override
    public void reset() {
        synchronized (lock) {
            Arrays.fill(records, 0);
            this.pointer = 0;
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
