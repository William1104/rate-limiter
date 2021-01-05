package one.williamwong.ratelimiter;

import java.time.Duration;

public class SynchronizedRateLimiter extends AbstractRateLimiter {

    private final Object lock;

    public SynchronizedRateLimiter(final int maxInvokes, final Duration duration) {
        super(maxInvokes, duration, 1);
        this.lock = new Object();
    }

    public SynchronizedRateLimiter(final int maxInvokes, final Duration duration, final int samplingInterval) {
        super(maxInvokes, duration, samplingInterval);
        this.lock = new Object();
    }

    @Override
    public long invoke() throws InterruptedException {
        synchronized (lock) {
            return record(pauseIfRequired());
        }
    }

    @Override
    public void reset() {
        synchronized (lock) {
            resetHistory();
        }
    }
}
