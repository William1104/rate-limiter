package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.concurrent.Callable;

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

    @Override public <T> T invoke(Callable<T> callable) throws Exception {
        synchronized (lock) {
            this.guavaRateLimiter.acquire();
            return callable.call();
        }
    }

    @Override public void invoke(Runnable runnable) throws Exception {
        synchronized (lock) {
            this.guavaRateLimiter.acquire();
            runnable.run();
        }
    }

    @Override public void reset() {
        synchronized (lock) {
            this.guavaRateLimiter = com.google.common.util.concurrent.RateLimiter.create(maxInvokesInASecond);
        }
    }
}
