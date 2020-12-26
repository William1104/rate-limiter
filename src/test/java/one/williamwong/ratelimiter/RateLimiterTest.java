package one.williamwong.ratelimiter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RateLimiterTest {

    private static final Duration DURATION = Duration.ofMillis(100);
    private static final int LIMIT = 10;

    private static IRateLimiter create(final Class<? extends IRateLimiter> rateLimiterClass,
                                       final ISleeper sleeper,
                                       final int limit,
                                       final Duration duration) throws Exception {
        return rateLimiterClass
                .getConstructor(ISleeper.class, int.class, Duration.class)
                .newInstance(sleeper, limit, duration);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            StampLockInstantArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
            SynchronizedInstantArrayRateLimiter.class
    })
    void within_rate_limit(Class<? extends IRateLimiter> rateLimiterClass) throws Exception {
        // setup rate limiter and sleeper
        final ISleeper sleeper = Mockito.spy(new Sleeper());
        final IRateLimiter rateLimiter = create(rateLimiterClass, sleeper, LIMIT, DURATION);

        // execute multiple 'acquire'
        // interval between acquire is right above the rate limit
        final long startTime = System.currentTimeMillis();
        assertThatCode(() -> {
            for (int i = 0; i < LIMIT * 2; i++) {
                NANOSECONDS.sleep(DURATION.toNanos() / LIMIT);
                rateLimiter.acquire();
            }
        }).doesNotThrowAnyException();
        long endTime = System.currentTimeMillis();

        // make sure the rate limiter slow down the speed to LIMIT/DURATION.
        assertThat((endTime - startTime)).isGreaterThanOrEqualTo(DURATION.toMillis() * 2);

        // make sure 'sleeper' is not called in this scenario.
        verify(sleeper, never()).sleepTill(any(Instant.class));
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            StampLockInstantArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
            SynchronizedInstantArrayRateLimiter.class
    })
    void excess_rate_limit(final Class<? extends IRateLimiter> rateLimiterClass) throws Exception {
        // setup rate limiter and sleeper
        final ISleeper sleeper = Mockito.spy(new Sleeper());
        final IRateLimiter rateLimiter = create(rateLimiterClass, sleeper, LIMIT, DURATION);

        // execute multiple 'acquire'
        // interval between acquire is below the rate limit
        final long sleepIntervalInNS = DURATION.toNanos() / LIMIT / 10;
        final long startTime = System.currentTimeMillis();
        assertThatCode(() -> {
            for (int i = 0; i < LIMIT * 10; i++) {
                NANOSECONDS.sleep(sleepIntervalInNS);
                rateLimiter.acquire();
            }
            rateLimiter.acquire();
        }).doesNotThrowAnyException();
        long endTime = System.currentTimeMillis();

        // make sure the rate limiter slow down the speed to LIMIT/DURATION.
        assertThat((endTime - startTime)).isGreaterThanOrEqualTo(DURATION.toMillis() * 10);

        // expecting sleeper to be invoked multiples times to slow down the process.
        verify(sleeper, atLeastOnce()).sleepTill(any(Instant.class));
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            StampLockInstantArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
            SynchronizedInstantArrayRateLimiter.class
    })
    void various_rate_limit(final Class<? extends IRateLimiter> rateLimiterClass) throws Exception {
        // setup rate limiter and sleeper
        final ISleeper sleeper = Mockito.spy(new Sleeper());
        final IRateLimiter rateLimiter = create(rateLimiterClass, sleeper, LIMIT, DURATION);

        // execute multiple 'acquire'
        // interval between acquire is not uniform, but we won't have more than LIMIT execution in last DURATION period.
        final long startTime = System.currentTimeMillis();
        assertThatCode(() -> {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < LIMIT; j++) {
                    rateLimiter.acquire();
                }
                NANOSECONDS.sleep(DURATION.toNanos() / LIMIT);
            }
            rateLimiter.acquire();
        }).doesNotThrowAnyException();
        long endTime = System.currentTimeMillis();

        // make sure the rate limiter slow down the speed to LIMIT/DURATION.
        assertThat((endTime - startTime)).isGreaterThanOrEqualTo(DURATION.toMillis() * 3);

    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            StampLockInstantArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
            SynchronizedInstantArrayRateLimiter.class
    })
    void reset_could_clear_counts(final Class<? extends IRateLimiter> rateLimiterClass) throws Exception {
        // setup rate limiter and sleeper
        final ISleeper sleeper = Mockito.spy(new Sleeper());
        final IRateLimiter rateLimiter = create(rateLimiterClass, sleeper, LIMIT, DURATION);

        // execute multiple 'acquire'
        // no sleep time between invocation. but all counter should be 'reset' after LIMIT of invocation.
        final long startTime = System.currentTimeMillis();
        assertThatCode(() -> {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < LIMIT; j++) {
                    rateLimiter.acquire();
                }
                rateLimiter.reset();
            }
        }).doesNotThrowAnyException();
        long endTime = System.currentTimeMillis();

        // make sure the rate limiter slow down the speed to LIMIT/DURATION.
        assertThat((endTime - startTime)).isLessThan(DURATION.toMillis());

        // make sure 'sleeper' is not called in this scenario.
        verify(sleeper, never()).sleepTill(any(Instant.class));
    }
}
