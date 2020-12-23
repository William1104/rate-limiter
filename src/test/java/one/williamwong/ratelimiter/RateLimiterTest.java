package one.williamwong.ratelimiter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

class RateLimiterTest {

    private static final Duration DURATION = ofSeconds(1);
    private static final int LIMIT = 10;

    @ParameterizedTest(name = "[{index}] - {0}")
    @ValueSource(strings = {
            "StampLockLongArrayRateLimiter",
            "StampLockInstantArrayRateLimiter",
            "SynchronizedLongArrayRateLimiter",
            "SynchronizedInstantArrayRateLimiter"})
    void within_rate_limit(String rateLimiterType) throws Exception {
        final IRateLimiter rateLimiter = create(rateLimiterType);
        for (int i = 0; i < LIMIT * 2; i++) {
            Thread.sleep(DURATION.toMillis() / LIMIT);
            rateLimiter.acquire();
        }
    }

    private static IRateLimiter create(final String rateLimiterClassName) throws Exception {
        final String packageName = IRateLimiter.class.getPackageName();
        return (IRateLimiter) Class.forName(packageName + "." + rateLimiterClassName)
                .getConstructor(int.class, Duration.class)
                .newInstance(LIMIT, DURATION);
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @ValueSource(strings = {
            "StampLockLongArrayRateLimiter",
            "StampLockInstantArrayRateLimiter",
            "SynchronizedLongArrayRateLimiter",
            "SynchronizedInstantArrayRateLimiter"})
    void excess_rate_limit(String rateLimiterType) throws InterruptedException {
        Assertions.assertThatCode(() -> {
            final IRateLimiter rateLimiter = create(rateLimiterType);
            for (int i = 0; i < LIMIT * 10; i++) {
                Thread.sleep(DURATION.toMillis() / LIMIT / 10);
                rateLimiter.acquire();
            }
        }).isInstanceOf(IRateLimiter.RateExcessException.class)
                .hasMessageContaining("excess rate limit");
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @ValueSource(strings = {
            "StampLockLongArrayRateLimiter",
            "StampLockInstantArrayRateLimiter",
            "SynchronizedLongArrayRateLimiter",
            "SynchronizedInstantArrayRateLimiter"})
    void various_rate_limit(String rateLimiterType) throws Exception {
        final IRateLimiter rateLimiter = create(rateLimiterType);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < LIMIT; j++) {
                rateLimiter.acquire();
            }
            Thread.sleep(DURATION.toMillis());
        }
    }
}
