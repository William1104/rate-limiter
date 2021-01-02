package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.concurrent.Callable;

import static java.lang.System.nanoTime;

public class GuavaRateLimiter implements RateLimiter {

    private final double maxInvokesInASecond;
    private final Object lock = new Object();
    private com.google.common.util.concurrent.RateLimiter guavaRateLimiter;

    public GuavaRateLimiter(final int maxInvokes, final Duration duration) {
        this.maxInvokesInASecond =
                (double) maxInvokes * (
                        (double) Duration.ofSeconds(1).toNanos() / (double) duration.toNanos());
        this.guavaRateLimiter = com.google.common.util.concurrent.RateLimiter.create(maxInvokesInASecond);
    }

    @Override
    public long invoke() throws InterruptedException {
        this.guavaRateLimiter.acquire();
        return nanoTime();
    }

    @Override
    public void reset() {
        synchronized (lock) {
            this.guavaRateLimiter = com.google.common.util.concurrent.RateLimiter.create(maxInvokesInASecond);
        }
    }
}
