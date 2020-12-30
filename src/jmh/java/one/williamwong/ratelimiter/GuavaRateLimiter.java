package one.williamwong.ratelimiter;

import com.google.common.util.concurrent.RateLimiter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class GuavaRateLimiter implements IRateLimiter {

    private final double maxInvokesInASecond;
    private final AtomicReference<RateLimiter> guavaRateLimiterRef;

    public GuavaRateLimiter(final int maxInvokes, final Duration duration) {
        this.maxInvokesInASecond =
                (double) maxInvokes * (
                        (double) duration.toNanos() / (double) Duration.ofSeconds(1).toNanos());
        this.guavaRateLimiterRef = new AtomicReference<>(RateLimiter.create(maxInvokesInASecond));
    }

    @Override public void acquire() throws InterruptedException {
        this.guavaRateLimiterRef.get().acquire();
    }

    @Override public void reset() {
        this.guavaRateLimiterRef.set(RateLimiter.create(maxInvokesInASecond));
    }
}
