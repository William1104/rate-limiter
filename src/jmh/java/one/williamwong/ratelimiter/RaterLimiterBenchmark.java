package one.williamwong.ratelimiter;

import org.openjdk.jmh.annotations.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class RaterLimiterBenchmark {

    @Group("thread_1")
    @GroupThreads(1)
    @Benchmark
    public void thread_1(RateLimiterWrapper rateLimiterWrapper) throws Exception {
        rateLimiterWrapper.rateLimiter.invoke();
    }

    @Group("thread_10")
    @GroupThreads(10)
    @Benchmark
    public void thread_10(RateLimiterWrapper rateLimiterWrapper) throws Exception {
        rateLimiterWrapper.rateLimiter.invoke();
    }

    @Group("thread_100")
    @GroupThreads(100)
    @Benchmark
    public void thread_100(RateLimiterWrapper rateLimiterWrapper) throws Exception {
        rateLimiterWrapper.rateLimiter.invoke();
    }

    @State(Scope.Group)
    public static class RateLimiterWrapper {
        @Param({"GuavaRateLimiter",
                "StampLockRateLimiter",
                "SynchronizedRateLimiter",
        })
        private String rateLimiterType;

        private RateLimiter rateLimiter;

        @Setup(Level.Iteration)
        public void setup() throws Exception {
            final String packageName = "one.williamwong.ratelimiter";
            rateLimiter = (RateLimiter) Class.forName(packageName + "." + rateLimiterType)
                    .getConstructor(int.class, Duration.class)
                    .newInstance(1_000, Duration.ofNanos(500));
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            rateLimiter.reset();
        }
    }

}
