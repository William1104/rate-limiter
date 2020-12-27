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
    public void thread_1(RateLimiterWrapper rateLimiterWrapper) throws InterruptedException {
        rateLimiterWrapper.rateLimiter.acquire();
    }

    @Group("thread_10")
    @GroupThreads(10)
    @Benchmark
    public void thread_10(RateLimiterWrapper rateLimiterWrapper) throws InterruptedException {
        rateLimiterWrapper.rateLimiter.acquire();
    }

    @Group("thread_100")
    @GroupThreads(100)
    @Benchmark
    public void thread_100(RateLimiterWrapper rateLimiterWrapper) throws InterruptedException {
        rateLimiterWrapper.rateLimiter.acquire();
    }

    @State(Scope.Group)
    public static class RateLimiterWrapper {
        @Param({"StampLockLongArrayRateLimiter",
                "StampLockInstantArrayRateLimiter",
                "SynchronizedLongArrayRateLimiter",
                "SynchronizedInstantArrayRateLimiter",
        })
        private String rateLimiterType;

        private IRateLimiter rateLimiter;

        @Setup(Level.Iteration)
        public void setup() throws Exception {
            final String packageName = IRateLimiter.class.getPackageName();
            rateLimiter = (IRateLimiter) Class.forName(packageName + "." + rateLimiterType)
                    .getConstructor(ISleeper.class, int.class, Duration.class)
                    .newInstance(new Sleeper(), 1_000_000, Duration.ofMillis(1));
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            rateLimiter.reset();
        }
    }

}
