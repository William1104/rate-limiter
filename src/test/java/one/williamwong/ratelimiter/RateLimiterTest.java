package one.williamwong.ratelimiter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    private static final Duration DURATION = Duration.ofMillis(100);
    private static final int LIMIT = 1000;

    private static RateLimiter create(final Class<? extends RateLimiter> rateLimiterClass,
                                      final int limit,
                                      final Duration duration) throws Exception {
        return rateLimiterClass
                .getConstructor(int.class, Duration.class)
                .newInstance(limit, duration);
    }

    /**
     * This method asserts the emitted rate should be lower than maxInvokes/duration.
     * However, as there is a little time difference between the measured emit time and actual time released
     * by a rate limiter, jitter could happen.
     * Therefore, this method accept a little percentage of calls happened in a higher rate.
     *
     * @param emitTimes
     * @param maxInvokes
     * @param duration
     */
    private static void assertEmitTimesDoesNotExcessRateLimit(
            final long[] emitTimes,
            final int maxInvokes,
            final long duration,
            final int percentageOfJitter) {

        long numOfJitter = 0;
        for (int i = 0; i < emitTimes.length - maxInvokes; i++) {
            final long timeTook = emitTimes[i + maxInvokes] - emitTimes[i];
            if (timeTook < duration) {
                numOfJitter++;
            }
        }
        Assertions.assertThat(numOfJitter)
                .describedAs("number of jitter (emit rate higher than expected) is more than %s percentage")
                .isLessThanOrEqualTo(emitTimes.length * percentageOfJitter / 100);
    }

    private static void assertEmitTimesDoesNotExcessRateLimit(
            final long[] emitTimes,
            final int maxInvokes,
            final long duration) {
        assertEmitTimesDoesNotExcessRateLimit(emitTimes, maxInvokes, duration, 0);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
    })
    void test_invoke_when_invocation_rate_within_limit(final Class<? extends RateLimiter> rateLimiterClass) throws Exception {
        // setup rate limiter and sleeper
        final RateLimiter rateLimiter = create(rateLimiterClass, LIMIT, DURATION);

        // execute multiple 'invoke'
        // interval between invoke is inverse of rate limit.
        final List<Long> emitTimes = new LinkedList<>();
        final long sleepInterval = DURATION.toNanos() / LIMIT;
        for (int i = 0; i < LIMIT * 2; i++) {
            NANOSECONDS.sleep(sleepInterval);
            rateLimiter.invoke();
            emitTimes.add(System.nanoTime());
        }

        // make sure the rate limiter slow down the speed.
        assertEmitTimesDoesNotExcessRateLimit(
                emitTimes.stream().mapToLong($ -> $).toArray(),
                LIMIT,
                DURATION.toNanos());
    }

    @ParameterizedTest
    @ValueSource(classes = {
            SynchronizedLongArrayRateLimiter.class,
            StampLockLongArrayRateLimiter.class,
    })
    void test_invoke_when_invocation_rate_excess_limit(final Class<? extends RateLimiter> rateLimiterClass) throws Exception {
        // setup rate limiter and sleeper
        final RateLimiter rateLimiter = create(rateLimiterClass, LIMIT, DURATION);

        final ExecutorService executor = Executors.newFixedThreadPool(10);

        // execute multiple 'invoke'
        // interval between invoke is less than inverse of rate limit.
        final List<Future<Long>> futures = new ArrayList<>();
        final LinkedBlockingQueue<Long> emitTimes = new LinkedBlockingQueue<>();
        for (int i = 0; i < LIMIT * 50; i++) {
            futures.add(executor.submit(() -> {
                rateLimiter.invoke();
                long current = System.nanoTime();
                emitTimes.add(current);
                return current;
            }));
        }

        for (Future<Long> future : futures) {
            future.get();
        }

        // make sure the rate limiter slow down the speed to LIMIT/DURATION.
        assertEmitTimesDoesNotExcessRateLimit(
                emitTimes.stream().mapToLong($ -> $).toArray(),
                LIMIT,
                DURATION.minus(Duration.ofNanos(100_000)).toNanos(),
                1);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
    })
    void test_invoke_when_invocation_rate_with_various_speed(final Class<? extends RateLimiter> rateLimiterClass) throws Exception {

        // setup rate limiter and sleeper
        final Random random = new Random(System.nanoTime());
        final RateLimiter rateLimiter = create(rateLimiterClass, LIMIT, DURATION);

        // execute multiple 'invoke'
        // interval between invoke is between 0ns and double of the inverse of rate limit.
        final List<Long> emitTimes = new LinkedList<>();
        final int maxSleepInterval = (int) (DURATION.toNanos() / LIMIT * 2);
        for (int i = 0; i < LIMIT * 3; i++) {
            NANOSECONDS.sleep(random.nextInt(maxSleepInterval));
            rateLimiter.invoke();
            emitTimes.add(System.nanoTime());
        }

        assertEmitTimesDoesNotExcessRateLimit(
                emitTimes.stream().mapToLong($ -> $).toArray(),
                LIMIT,
                DURATION.toNanos());
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
    })
    void test_reset_which_can_reset_invocation_history(final Class<? extends RateLimiter> rateLimiterClass) throws Exception {
        // setup rate limiter and sleeper
        final RateLimiter rateLimiter = create(rateLimiterClass, LIMIT, DURATION);

        // execute multiple 'invoke'
        // no sleep time between invocation. but all counter should be 'reset' after LIMIT of invocation.
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < LIMIT; j++) {
                rateLimiter.invoke();
            }
            rateLimiter.reset();
        }
        long endTime = System.currentTimeMillis();

        // make sure the rate limiter slow down the speed to LIMIT/DURATION.
        assertThat((endTime - startTime)).isLessThan(DURATION.toMillis() * 3);
    }
}
