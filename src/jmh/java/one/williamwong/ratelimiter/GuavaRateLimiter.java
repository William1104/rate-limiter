package one.williamwong.ratelimiter;

import com.google.common.util.concurrent.RateLimiter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class GuavaRateLimiter implements IRateLimiter {

    private final double maxInvokesInASecond;
    private final AtomicReference<RateLimiter> guavaRateLimiterRef;

    public GuavaRateLimiter(final int maxInvokesInASecond, final Duration duration) {
        this.maxInvokesInASecond =
                maxInvokesInASecond * (
                        duration.toNanos() / 1_000_000.0);
        this.guavaRateLimiterRef = new AtomicReference<>(RateLimiter.create(maxInvokesInASecond));
    }

    @Override public void acquire() throws InterruptedException {
        this.guavaRateLimiterRef.get().acquire();
    }

    @Override public void reset() {
        this.guavaRateLimiterRef.set(RateLimiter.create(maxInvokesInASecond));
    }
}
