package one.williamwong.ratelimiter;

import org.openjdk.jmh.annotations.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(MICROSECONDS)
public class RaterLimiterBenchmark {

    @Group("thread_1")
    @GroupThreads(1)
    @Benchmark
    public void thread_1(RateLimiterWrapper rateLimiterWrapper) {
        rateLimiterWrapper.rateLimiter.acquire();
    }

    @Group("thread_10")
    @GroupThreads(10)
    @Benchmark
    public void thread_10(RateLimiterWrapper rateLimiterWrapper) {
        rateLimiterWrapper.rateLimiter.acquire();
    }

    @Group("thread_100")
    @GroupThreads(100)
    @Benchmark
    public void thread_100(RateLimiterWrapper rateLimiterWrapper) {
        rateLimiterWrapper.rateLimiter.acquire();
    }

    @State(Scope.Group)
    public static class RateLimiterWrapper {
        @Param({"StampLockInstantArrayRateLimiter",
                "StampLockLongArrayRateLimiter",
                "SynchronizedInstantArrayRateLimiter",
                "SynchronizedLongArrayRateLimiter"})

        public String rateLimiterType;

        public IRateLimiter rateLimiter;

        @Setup(Level.Iteration)
        public void setup() throws Exception {
            final String packageName = IRateLimiter.class.getPackageName();
            rateLimiter = (IRateLimiter) Class.forName(packageName + "." + rateLimiterType)
                    .getConstructor(int.class, Duration.class)
                    .newInstance(1_000_000, Duration.ofNanos(1));
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            rateLimiter.reset();
        }
    }

}
