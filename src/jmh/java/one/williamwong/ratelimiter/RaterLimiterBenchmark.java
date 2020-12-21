package one.williamwong.ratelimiter;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(MICROSECONDS)
public class RaterLimiterBenchmark {
    @Param({"1", "10", "100"})
    private Integer numOfInvokers;

    @Param({"100", "10000", "1000000"})
    private Integer maxInvokes;

    @Param({"10", "1000"})
    private Long durationInMs;

    @Param({"SimpleRateLimiter", "StampLockRateLimiter"})
    private String rateLimiterType;

    private IRateLimiter rateLimiter;

    @Setup
    public void setup() {
        switch (rateLimiterType) {
            case "SimpleRateLimiter":
                rateLimiter = new SimpleRateLimiter(maxInvokes, Duration.ofMillis(durationInMs));
                break;
            case "StampLockRateLimiter":
                rateLimiter = new StampLockRateLimiter(maxInvokes, Duration.ofMillis(durationInMs));
                break;

            default:
                throw new IllegalStateException("Failed to initialize rate limiter");
        }
    }

    @Benchmark
    public void test(Blackhole bh) throws ExecutionException, InterruptedException {

        rateLimiter.reset();
        List<CompletableFuture> futures = new ArrayList<>();

        for (int i = 0; i < numOfInvokers; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                int numOfInvocations = maxInvokes / numOfInvokers;
                for (int j = 0; j < numOfInvocations; j++) {
                    rateLimiter.acquire();
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[1])).get();
    }

}
