package one.williamwong.ratelimiter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.time.Duration.between;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    private static final Duration DURATION = Duration.ofMillis(100);
    private static final int LIMIT = 10;

    private static IRateLimiter create(final Class<? extends IRateLimiter> rateLimiterClass,
                                       final int limit,
                                       final Duration duration) throws Exception {
        return rateLimiterClass
                .getConstructor(int.class, Duration.class)
                .newInstance(limit, duration);
    }

    private static void assertEmitTimesDoesNotExcessRateLimit(List<Instant> emitTimes) {
        // calculate the time different between LIMIT calls.
        // the time difference between them should be bigger than DURATION
        for (int i = 0; i < emitTimes.size() - LIMIT - 1; i++) {
            final Duration timeTook = between(emitTimes.get(i), emitTimes.get(i + LIMIT + 1));
            assertThat(timeTook).isGreaterThanOrEqualTo(DURATION);
        }
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
        final IRateLimiter rateLimiter = create(rateLimiterClass, LIMIT, DURATION);

        // execute multiple 'acquire'
        // interval between acquire is inverse of rate limit.
        final List<Instant> emitTimes = new ArrayList<>();
        final long sleepInterval = DURATION.toNanos() / LIMIT;
        for (int i = 0; i < LIMIT * 2; i++) {
            NANOSECONDS.sleep(sleepInterval);
            rateLimiter.acquire();
            emitTimes.add(Instant.now());
        }

        // make sure the rate limiter slow down the speed.
        assertEmitTimesDoesNotExcessRateLimit(emitTimes);
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
        final IRateLimiter rateLimiter = create(rateLimiterClass, LIMIT, DURATION);

        // execute multiple 'acquire'
        // interval between acquire is less than inverse of rate limit.
        final long sleepIntervalInNS = DURATION.toNanos() / LIMIT / 10;
        final List<Instant> emitTimes = new ArrayList<>();
        for (int i = 0; i < LIMIT * 10; i++) {
            NANOSECONDS.sleep(sleepIntervalInNS);
            rateLimiter.acquire();
        }

        // make sure the rate limiter slow down the speed to LIMIT/DURATION.
        assertEmitTimesDoesNotExcessRateLimit(emitTimes);
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
        final Random random = new Random(System.nanoTime());
        final IRateLimiter rateLimiter = create(rateLimiterClass, LIMIT, DURATION);

        // execute multiple 'acquire'
        // interval between acquire is between 0ns and double of the inverse of rate limit.
        final List<Instant> emitTimes = new ArrayList<>();
        final int maxSleepInterval = (int) (DURATION.toNanos() / LIMIT * 2);
        for (int i = 0; i < LIMIT * 3; i++) {
            NANOSECONDS.sleep(random.nextInt(maxSleepInterval));
            rateLimiter.acquire();
            emitTimes.add(Instant.now());
        }

        assertEmitTimesDoesNotExcessRateLimit(emitTimes);
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
        final IRateLimiter rateLimiter = create(rateLimiterClass, LIMIT, DURATION);

        // execute multiple 'acquire'
        // no sleep time between invocation. but all counter should be 'reset' after LIMIT of invocation.
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < LIMIT; j++) {
                rateLimiter.acquire();
            }
            rateLimiter.reset();
        }
        long endTime = System.currentTimeMillis();

        // make sure the rate limiter slow down the speed to LIMIT/DURATION.
        assertThat((endTime - startTime)).isLessThan(DURATION.toMillis());
    }
}
