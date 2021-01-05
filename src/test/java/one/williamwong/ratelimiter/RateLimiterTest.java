package one.williamwong.ratelimiter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

class RateLimiterTest {

    private static final Duration DURATION = Duration.ofMillis(100);
    private static final int LIMIT = 1000;

    static Stream<Arguments> arguments() {
        return Stream.of(
                of(new StampLockRateLimiter(LIMIT, DURATION, 1), LIMIT, DURATION, 1),
                of(new StampLockRateLimiter(LIMIT, DURATION, 10), LIMIT, DURATION, 10),
                of(new StampLockRateLimiter(LIMIT, DURATION, 100), LIMIT, DURATION, 100),
                of(new SynchronizedRateLimiter(LIMIT, DURATION, 1), LIMIT, DURATION, 1),
                of(new SynchronizedRateLimiter(LIMIT, DURATION, 10), LIMIT, DURATION, 10),
                of(new SynchronizedRateLimiter(LIMIT, DURATION, 100), LIMIT, DURATION, 100));
    }

    /**
     * This method asserts the released rate should be lower than maxInvokes/duration.
     * However, as there is a little time difference between the measured release time and actual time released
     * by a rate limiter, jitter could happen.`
     * Therefore, this method accept a little percentage of calls happened in a higher rate.
     *
     * @param releaseTimes
     * @param maxInvokes
     * @param duration
     */
    private static void assertEmitTimesDoesNotExcessRateLimit(
            final long[] releaseTimes,
            final int maxInvokes,
            final long duration,
            final int samplingInterval,
            final int percentageOfJitter) {

        Arrays.sort(releaseTimes);

        int numOfJitter = 0;
        for (int i = 0; i < releaseTimes.length - maxInvokes; i++) {
            final long timeTook = releaseTimes[i + maxInvokes] - releaseTimes[i];
            if (timeTook < duration - duration / (maxInvokes / samplingInterval)) {
                numOfJitter++;
            }
        }
        Assertions.assertThat(numOfJitter)
                .describedAs("number of jitter (release rate higher than expected) is more than %s percentage", percentageOfJitter)
                .isLessThanOrEqualTo(releaseTimes.length * percentageOfJitter / 100);
    }

    private static void assertEmitTimesDoesNotExcessRateLimit(
            final long[] releaseTimes,
            final int maxInvokes,
            final long duration,
            final int samplingInterval) {
        assertEmitTimesDoesNotExcessRateLimit(releaseTimes, maxInvokes, duration, samplingInterval, 0);
    }

    @ParameterizedTest(name = "{index}: test_invoke_when_invocation_rate_within_limit({arguments})")
    @MethodSource(value = "arguments")
    void test_invoke_when_invocation_rate_within_limit(
            final RateLimiter rateLimiter,
            final int maxInvokes,
            final Duration duration,
            final int samplingInterval
    ) throws Exception {

        // execute multiple 'invoke'
        // interval between invoke is inverse of rate limit.
        final List<Long> releaseTimes = new LinkedList<>();
        final long sleepInterval = duration.toNanos() / maxInvokes;
        for (int i = 0; i < maxInvokes * 2; i++) {
            NANOSECONDS.sleep(sleepInterval);
            rateLimiter.invoke();
            releaseTimes.add(nanoTime());
        }

        // make sure the rate limiter slow down the speed.
        assertEmitTimesDoesNotExcessRateLimit(
                releaseTimes.stream().mapToLong($ -> $).toArray(),
                maxInvokes,
                duration.toNanos(),
                samplingInterval);
    }

    @ParameterizedTest(name = "{index}: test_invoke_when_invocation_rate_excess_limit({arguments})")
    @MethodSource(value = "arguments")
    void test_invoke_when_invocation_rate_excess_limit(
            final RateLimiter rateLimiter,
            final int maxInvokes,
            final Duration duration,
            final int samplingInterval) throws Exception {

        final ExecutorService executor = Executors.newFixedThreadPool(10);

        // execute multiple 'invoke'
        // interval between invoke is less than inverse of rate limit.
        final List<Future<Long>> futures = new ArrayList<>();
        final LinkedBlockingQueue<Long> releaseTimes = new LinkedBlockingQueue<>();
        final int numOfInvokes = maxInvokes * 50;
        for (int i = 0; i < numOfInvokes; i++) {
            futures.add(executor.submit(() -> {
                long current = rateLimiter.invoke();
                releaseTimes.add(current);
                return current;
            }));
        }

        for (Future<Long> future : futures) {
            future.get();
        }

        // make sure the rate limiter slow down the speed to LIMIT/DURATION.
        assertEmitTimesDoesNotExcessRateLimit(
                releaseTimes.stream().mapToLong($ -> $).toArray(),
                maxInvokes,
                duration.toNanos(),
                samplingInterval,
                1);
    }

    @ParameterizedTest(name = "{index}: test_invoke_when_invocation_rate_with_various_speed({arguments})")
    @MethodSource(value = "arguments")
    void test_invoke_when_invocation_rate_with_various_speed(
            final RateLimiter rateLimiter,
            final int maxInvokes,
            final Duration duration,
            final int samplingInterval) throws Exception {

        // setup rate limiter and sleeper
        final Random random = new Random(nanoTime());

        // execute multiple 'invoke'
        // interval between invoke is between 0ns and double of the inverse of rate limit.
        final List<Long> releaseTimes = new LinkedList<>();
        final int maxSleepInterval = (int) (duration.toNanos() / maxInvokes * 2);
        for (int i = 0; i < maxInvokes * 3; i++) {
            NANOSECONDS.sleep(random.nextInt(maxSleepInterval));
            rateLimiter.invoke();
            releaseTimes.add(nanoTime());
        }

        assertEmitTimesDoesNotExcessRateLimit(
                releaseTimes.stream().mapToLong($ -> $).toArray(),
                maxInvokes,
                duration.toNanos(),
                samplingInterval);
    }

    @ParameterizedTest(name = "{index}: test_reset_which_can_reset_invocation_history({arguments})")
    @MethodSource(value = "arguments")
    void test_reset_which_can_reset_invocation_history(
            final RateLimiter rateLimiter,
            final int maxInvokes,
            final Duration duration,
            final int samplingInterval) throws Exception {

        // execute multiple 'invoke'
        // no sleep time between invocation. but all counter should be 'reset' after LIMIT of invocation.
        final long startTime = currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < maxInvokes; j++) {
                rateLimiter.invoke();
            }
            rateLimiter.reset();
        }
        long endTime = currentTimeMillis();

        // make sure the rate limiter slow down the speed to LIMIT/DURATION.
        assertThat((endTime - startTime)).isLessThan(duration.toMillis() * 3);
    }
}
