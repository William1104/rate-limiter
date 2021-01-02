package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static java.lang.System.nanoTime;

public class SynchronizedRateLimiter extends AbstractRateLimiter {

    private final Object lock;

    public SynchronizedRateLimiter(final int maxInvokes, final Duration duration) {
        super(maxInvokes, duration);
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
