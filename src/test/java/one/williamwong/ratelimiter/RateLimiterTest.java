package one.williamwong.ratelimiter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static java.time.Duration.between;
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

    private static void assertEmitTimesDoesNotExcessRateLimit(List<Instant> emitTimes, int maxInvokes, Duration duration) {
        // calculate the time different between LIMIT calls.
        // the time difference between them should be bigger than DURATION
        for (int i = 0; i < emitTimes.size() - maxInvokes; i++) {
            final Duration timeTook = between(emitTimes.get(i), emitTimes.get(i + maxInvokes));
            assertThat(timeTook).isGreaterThanOrEqualTo(duration);
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
    })
    void within_rate_limit(final Class<? extends RateLimiter> rateLimiterClass) throws Exception {
        // setup rate limiter and sleeper
        final RateLimiter rateLimiter = create(rateLimiterClass, LIMIT, DURATION);

        // execute multiple 'invoke'
        // interval between invoke is inverse of rate limit.
        final List<Instant> emitTimes = new ArrayList<>();
        final long sleepInterval = DURATION.toNanos() / LIMIT;
        for (int i = 0; i < LIMIT * 2; i++) {
            NANOSECONDS.sleep(sleepInterval);
            rateLimiter.invoke(() -> emitTimes.add(Instant.now()));
        }

        // make sure the rate limiter slow down the speed.
        assertEmitTimesDoesNotExcessRateLimit(emitTimes, LIMIT, DURATION);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            SynchronizedLongArrayRateLimiter.class,
            StampLockLongArrayRateLimiter.class,
    })
    void excess_rate_limit(final Class<? extends RateLimiter> rateLimiterClass) throws Exception {
        // setup rate limiter and sleeper
        final RateLimiter rateLimiter = create(rateLimiterClass, LIMIT, DURATION);

        final ExecutorService executor = Executors.newFixedThreadPool(10);

        // execute multiple 'invoke'
        // interval between invoke is less than inverse of rate limit.
        final List<Future<Instant>> futures = new ArrayList<>();
        final LinkedBlockingQueue<Instant> emitTimes = new LinkedBlockingQueue<>();
        for (int i = 0; i < LIMIT * 50; i++) {
            futures.add(executor.submit(() -> rateLimiter.invoke(() -> {
                Instant instant = Instant.now();
                emitTimes.add(instant);
                return instant;
            })));
        }

        for (Future<Instant> future : futures) {
            future.get();
        }

        // make sure the rate limiter slow down the speed to LIMIT/DURATION.
        assertEmitTimesDoesNotExcessRateLimit(
                emitTimes.stream().collect(Collectors.toList()),
                LIMIT,
                DURATION.minus(Duration.ofMillis(2)));
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
    })
    void various_rate_limit(final Class<? extends RateLimiter> rateLimiterClass) throws Exception {

        // setup rate limiter and sleeper
        final Random random = new Random(System.nanoTime());
        final RateLimiter rateLimiter = create(rateLimiterClass, LIMIT, DURATION);

        // execute multiple 'invoke'
        // interval between invoke is between 0ns and double of the inverse of rate limit.
        final List<Instant> emitTimes = new ArrayList<>();
        final int maxSleepInterval = (int) (DURATION.toNanos() / LIMIT * 2);
        for (int i = 0; i < LIMIT * 3; i++) {
            NANOSECONDS.sleep(random.nextInt(maxSleepInterval));
            rateLimiter.invoke(() -> emitTimes.add(Instant.now()));
        }

        assertEmitTimesDoesNotExcessRateLimit(emitTimes, LIMIT, DURATION);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            StampLockLongArrayRateLimiter.class,
            SynchronizedLongArrayRateLimiter.class,
    })
    void reset_could_clear_counts(final Class<? extends RateLimiter> rateLimiterClass) throws Exception {
        // setup rate limiter and sleeper
        final RateLimiter rateLimiter = create(rateLimiterClass, LIMIT, DURATION);

        // execute multiple 'invoke'
        // no sleep time between invocation. but all counter should be 'reset' after LIMIT of invocation.
        final long startTime = System.currentTimeMillis();
        final List<Instant> emitTimes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < LIMIT; j++) {
                rateLimiter.invoke(() -> emitTimes.add(Instant.now()));
            }
            rateLimiter.reset();
        }
        long endTime = System.currentTimeMillis();

        // make sure the rate limiter slow down the speed to LIMIT/DURATION.
        assertThat((endTime - startTime)).isLessThan(DURATION.toMillis() * 3);
    }
}
