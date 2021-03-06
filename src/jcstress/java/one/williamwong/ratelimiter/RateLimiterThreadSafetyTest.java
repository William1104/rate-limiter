package one.williamwong.ratelimiter;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

class RateLimiterWrapper<T extends RateLimiter> {
    private final T rateLimiter;
    private final BlockingQueue<Long> emitTimes;
    private final AtomicInteger numOfActiveActors;
    private final AtomicInteger numOfInvokes;
    private final AtomicInteger numOfJitters;

    RateLimiterWrapper(T rateLimiter, int numOfActors) {
        try {
            this.rateLimiter = rateLimiter;
            this.numOfActiveActors = new AtomicInteger(numOfActors);
            this.numOfInvokes = new AtomicInteger(0);
            this.numOfJitters = new AtomicInteger(0);
            this.emitTimes = new LinkedBlockingDeque<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void verifyIfEmitTimeExcessExpectedRate(final int maxInvokes, final long duration) {
        while (numOfActiveActors.get() > 0) {
            final long[] instants = emitTimes.stream().mapToLong($ -> $).toArray();
            for (int i = 0; i < instants.length - maxInvokes; i++) {
                final long timeTook = instants[i + maxInvokes] - instants[i];
                if (timeTook < duration) {
                    numOfJitters.incrementAndGet();
                }
                emitTimes.remove();
            }
        }
    }

    void verifyJitterPercentageOverThreshold(int threshold) {
        int numOfJitterAsInt = numOfJitters.get();
        int numOfInvokesAsInt = numOfInvokes.get();
        if (numOfJitterAsInt * 100 > numOfInvokesAsInt * threshold) {
            throw new IllegalStateException("" + numOfJitterAsInt + " of jitter happened in " + numOfInvokesAsInt + " invocation");
        }
    }

    void reduceActiveActor() {
        numOfActiveActors.decrementAndGet();
    }

    void record() {
        numOfInvokes.incrementAndGet();
        emitTimes.add(System.nanoTime());
    }

    T getRateLimiter() {
        return rateLimiter;
    }
}

public class RateLimiterThreadSafetyTest {

    public static final int MAX_INVOKES = 1000;
    public static final Duration DURATION = Duration.ofMillis(1);

    private static <T extends RateLimiter>
    void invoke(RateLimiterWrapper<T> wrapper, L_Result result) {
        final RateLimiter rateLimiter = wrapper.getRateLimiter();
        try {
            for (int i = 0; i < MAX_INVOKES; i++) {
                rateLimiter.invoke();
                wrapper.record();
            }
        } catch (Exception e) {
            result.r1 = e;
        }
        wrapper.reduceActiveActor();
    }

    private static <T extends RateLimiter>
    void checkEmittedTimes(RateLimiterWrapper<T> wrapper, L_Result result) {
        try {
            wrapper.verifyIfEmitTimeExcessExpectedRate(MAX_INVOKES, DURATION.toNanos());
        } catch (Exception e) {
            result.r1 = e;
        }
    }

    private static <T extends RateLimiter>
    void checkJitterPercentage(RateLimiterWrapper<T> wrapper, L_Result result) {
        try {
            wrapper.verifyJitterPercentageOverThreshold(1);
        } catch (Exception e) {
            result.r1 = e;
        }
    }

    @JCStressTest
    @Description("Test race on concurrent invoke")
    @Outcome(id = "null", expect = ACCEPTABLE, desc = "all records are increasing")
    @Outcome(id = ".*", expect = FORBIDDEN, desc = "hit exception during invoke")
    public static class StampLockLongArrayRateLimiterInvokeTest {

        @Actor
        public void actor1(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor2(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor3(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor4(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor5(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor6(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor7(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor8(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void checkActor(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            checkEmittedTimes(wrapper, result);
        }

        @Arbiter
        public void finalCheckActor(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            checkJitterPercentage(wrapper, result);
        }

        @State
        public static class StampLockLongArrayRateLimiterWrapper extends RateLimiterWrapper<StampLockRateLimiter> {
            StampLockLongArrayRateLimiterWrapper() {
                super(new StampLockRateLimiter(MAX_INVOKES, DURATION), 8);
            }
        }
    }

    @JCStressTest
    @Description("Test race on concurrent invoke")
    @Outcome(id = "null", expect = ACCEPTABLE, desc = "all records are increasing")
    @Outcome(id = ".*", expect = FORBIDDEN, desc = "hit exception during invoke")
    public static class SynchronizedLongArrayRateLimiterInvokeTest {

        @Actor
        public void actor1(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor2(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor3(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor4(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor5(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor6(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor7(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor8(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void checkActor(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            checkEmittedTimes(wrapper, result);
        }

        @Arbiter
        public void finalCheckActor(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            checkJitterPercentage(wrapper, result);
        }

        @State
        public static class SynchronizedLongArrayRateLimiterWrapper extends RateLimiterWrapper<SynchronizedRateLimiter> {
            SynchronizedLongArrayRateLimiterWrapper() {
                super(new SynchronizedRateLimiter(MAX_INVOKES, DURATION), 8);
            }
        }
    }
}


