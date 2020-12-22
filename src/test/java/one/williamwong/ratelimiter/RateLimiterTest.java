package one.williamwong.ratelimiter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static java.time.Duration.ofSeconds;

class RateLimiterTest {

    private static Stream<Arguments> arguments() {
        return Stream.of(
                Arguments.of("StampLockLongArrayRateLimiter", new StampLockLongArrayRateLimiter(10, ofSeconds(1))),
                Arguments.of("StampLockInstantArrayRateLimiter", new StampLockInstantArrayRateLimiter(10, ofSeconds(1))),
                Arguments.of("SynchronizedLongArrayRateLimiter", new SynchronizedLongArrayRateLimiter(10, ofSeconds(1))),
                Arguments.of("SynchronizedInstantArrayRateLimiter", new SynchronizedInstantArrayRateLimiter(10, ofSeconds(1)))
        );
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @MethodSource("arguments")
    void within_rate_limit(String rateLimiterType, IRateLimiter rateLimiter) throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            Thread.sleep(100);
            rateLimiter.acquire();
        }
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @MethodSource("arguments")
    void excess_rate_limit(String rateLimiterType, IRateLimiter rateLimiter) throws InterruptedException {
        Assertions.assertThatCode(() -> {
            for (int i = 0; i < 1000; i++) {
                Thread.sleep(10);
                rateLimiter.acquire();
            }
        }).isInstanceOf(IRateLimiter.RateExcessException.class).hasMessage("excess rate limit");
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @MethodSource("arguments")
    void various_rate_limit(String rateLimiterType, IRateLimiter rateLimiter) throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 10; j++) {
                rateLimiter.acquire();
            }
            Thread.sleep(1000);
        }
    }
}
