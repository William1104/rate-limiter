package one.williamwong.ratelimiter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

class RateLimiterTest {

    private static final Duration DURATION = Duration.ofMillis(100);
    private static final int LIMIT = 10;

    private static IRateLimiter create(final Class<? extends IRateLimiter> rateLimiterClass) throws Exception {
        if (rateLimiterClass.equals(StampLockLongArrayRateLimiter.class)) {
            return new StampLockLongArrayRateLimiter(LIMIT, DURATION);
        }
        if (rateLimiterClass.equals(StampLockInstantArrayRateLimiter.class)) {
            return new StampLockInstantArrayRateLimiter(LIMIT, DURATION);
        }
        if (rateLimiterClass.equals(SynchronizedLongArrayRateLimiter.class)) {
            return new SynchronizedLongArrayRateLimiter(LIMIT, DURATION);
        }
        if (rateLimiterClass.equals(SynchronizedInstantArrayRateLimiter.class)) {
            return new SynchronizedInstantArrayRateLimiter(LIMIT, DURATION);
        }
        throw new IllegalArgumentException("Cannot prepare the rate limiter with class[" + rateLimiterClass + "]");
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            StampLockInstantArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
            SynchronizedInstantArrayRateLimiter.class})
    void within_rate_limit(Class<? extends IRateLimiter> rateLimitClass) throws Exception {
        Assertions.assertThatCode(() -> {
            final IRateLimiter rateLimiter = create(rateLimitClass);
            for (int i = 0; i < LIMIT * 2; i++) {
                Thread.sleep(DURATION.toMillis() / LIMIT);
                rateLimiter.acquire();
            }
        }).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            StampLockInstantArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
            SynchronizedInstantArrayRateLimiter.class})
    void excess_rate_limit(final Class<? extends IRateLimiter> rateLimitClass) {
        Assertions.assertThatCode(() -> {
            final IRateLimiter rateLimiter = create(rateLimitClass);
            for (int i = 0; i < LIMIT * 10; i++) {
                Thread.sleep(DURATION.toMillis() / LIMIT / 10);
                rateLimiter.acquire();
            }
        }).isInstanceOf(IRateLimiter.RateExcessException.class)
                .hasMessageContaining("excess rate limit");
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            StampLockInstantArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
            SynchronizedInstantArrayRateLimiter.class})
    void various_rate_limit(final Class<? extends IRateLimiter> rateLimitClass) {
        Assertions.assertThatCode(() -> {
            final IRateLimiter rateLimiter = create(rateLimitClass);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < LIMIT; j++) {
                    rateLimiter.acquire();
                }
                Thread.sleep(DURATION.toMillis() + 1);
            }
        }).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            StampLockInstantArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
            SynchronizedInstantArrayRateLimiter.class})
    void reset_could_clear_counts(final Class<? extends IRateLimiter> rateLimitClass) {
        Assertions.assertThatCode(() -> {
            final IRateLimiter rateLimiter = create(rateLimitClass);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < LIMIT; j++) {
                    rateLimiter.acquire();
                }
                rateLimiter.reset();
            }
        }).doesNotThrowAnyException();
    }
}
