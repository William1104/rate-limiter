package one.williamwong.ratelimiter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

class SimpleRateLimiterTest {

    @Test
    void within_time_limit() throws InterruptedException {
        SimpleRateLimiter rateLimiter = new SimpleRateLimiter(10, Duration.ofSeconds(1));
        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);
            rateLimiter.acquire();
        }
    }

    @Test
    void excess_the_limit() {
        SimpleRateLimiter rateLimiter = new SimpleRateLimiter(10, Duration.ofSeconds(1));
        Assertions.assertThatCode(() -> {
            for (int i = 0; i < 1000; i++) {
                Thread.sleep(10);
                rateLimiter.acquire();
            }
        }).isInstanceOf(SimpleRateLimiter.RateLimitExcessException.class).hasMessage("excess rate limit");
    }

    @Test
    void with_various_rate() throws InterruptedException {
        SimpleRateLimiter rateLimiter = new SimpleRateLimiter(10, Duration.ofSeconds(1));
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                rateLimiter.acquire();
            }
            Thread.sleep(1000);
        }
    }
}
