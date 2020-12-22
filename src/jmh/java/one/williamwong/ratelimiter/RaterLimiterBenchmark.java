package one.williamwong.ratelimiter;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MICROSECONDS;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(MICROSECONDS)
public class RaterLimiterBenchmark {

    @Group("singleThread")
    @GroupThreads(1)
    @Benchmark
    public void singleThread(RateLimiterWrapper rateLimiterWrapper) {
        rateLimiterWrapper.rateLimiter.acquire();
    }

    @Group("tenThread")
    @GroupThreads(10)
    @Benchmark
    public void tenThread(RateLimiterWrapper rateLimiterWrapper) {
        rateLimiterWrapper.rateLimiter.acquire();
    }

    @Group("hundredThread")
    @GroupThreads(100)
    @Benchmark
    public void hundredThread(RateLimiterWrapper rateLimiterWrapper) {
        rateLimiterWrapper.rateLimiter.acquire();
    }


    @State(Scope.Group)
    public static class RateLimiterWrapper {
        @Param({"SimpleRateLimiter", "StampLockRateLimiter"})
        public String rateLimiterType;

        public IRateLimiter rateLimiter;

        @Setup
        public void setup() {
            switch (rateLimiterType) {
                case "SimpleRateLimiter":
                    rateLimiter = new SimpleRateLimiter(1_000_000, ofMillis(1));
                    break;
                case "StampLockRateLimiter":
                    rateLimiter = new StampLockRateLimiter(1_000_000, ofMillis(1));
                    break;
                default:
                    throw new IllegalStateException("Failed to initialize rate limiter");
            }
        }
    }

}
