package one.williamwong.ratelimiter;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.between;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

class RateLimiterWrapper<T extends RateLimiter> {
    private final T rateLimiter;
    private final BlockingQueue<Instant> emitTimes;
    private final AtomicInteger numOfActiveActors;

    RateLimiterWrapper(T rateLimiter, int numOfActors) {
        try {
            this.rateLimiter = rateLimiter;
            this.numOfActiveActors = new AtomicInteger(numOfActors);
            this.emitTimes = new LinkedBlockingDeque<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void verifyIfEmitTimeExcessExpectedRate(final int maxInvokes, final Duration duration) {
        while (numOfActiveActors.get() > 0) {
            final Instant[] instants = emitTimes.toArray(new Instant[0]);
            for (int i = 0; i < instants.length - maxInvokes; i++) {
                final Duration timeTook = between(instants[i], instants[i + maxInvokes]);
                if (timeTook.compareTo(duration) < 0) {
                    throw new IllegalStateException("There are " + maxInvokes + " invokes took " + timeTook);
                } else {
                    emitTimes.remove();
                }
            }
        }
    }

    void reduceActiveActor() {
        numOfActiveActors.decrementAndGet();
    }

    void record(Instant instant) {
        emitTimes.add(instant);
    }

    T getRateLimiter() {
        return rateLimiter;
    }
}

public class RateLimiterThreadSafetyTest {

    public static final Random RANDOM = new Random(System.nanoTime());
    public static final int MAX_INVOKES = 1000;
    public static final Duration DURATION = Duration.ofMillis(1);

    private static <T extends RateLimiter>
    void invoke(RateLimiterWrapper<T> wrapper, L_Result result) {
        final RateLimiter rateLimiter = wrapper.getRateLimiter();
        try {
            for (int i = 0; i < MAX_INVOKES; i++) {
                rateLimiter.invoke(() -> wrapper.record(Instant.now()));
            }
        } catch (Exception e) {
            result.r1 = e;
        }
        wrapper.reduceActiveActor();
    }

    private static <T extends RateLimiter>
    void checkEmittedTimes(RateLimiterWrapper<T> wrapper, L_Result result) {
        try {
            wrapper.verifyIfEmitTimeExcessExpectedRate(MAX_INVOKES, DURATION.minus(Duration.ofMillis(1)));
        } catch (Exception e) {
            result.r1 = e;
        }
    }

    @JCStressTest
    @Description("Test race on concurrent invoke")
    @Outcome(id = "null", expect = ACCEPTABLE, desc = "all records are increasing")
    @Outcome(id = ".*", expect = FORBIDDEN, desc = "hit exception during invoke")
    public static class StampLockInstantArrayRateLimiterInvokeTest {

        @Actor
        public void actor1(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor2(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor3(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor4(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor5(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor6(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor7(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor8(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void checkActor(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            checkEmittedTimes(wrapper, result);
        }

        @State
        public static class StampLockInstantArrayRateLimiterWrapper extends RateLimiterWrapper<StampLockInstantArrayRateLimiter> {
            StampLockInstantArrayRateLimiterWrapper() {
                super(new StampLockInstantArrayRateLimiter(MAX_INVOKES, DURATION), 8);
            }
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

        @State
        public static class StampLockLongArrayRateLimiterWrapper extends RateLimiterWrapper<StampLockLongArrayRateLimiter> {
            StampLockLongArrayRateLimiterWrapper() {
                super(new StampLockLongArrayRateLimiter(MAX_INVOKES, DURATION), 8);
            }
        }
    }

    @JCStressTest
    @Description("Test race on concurrent invoke")
    @Outcome(id = "null", expect = ACCEPTABLE, desc = "all records are increasing")
    @Outcome(id = ".*", expect = FORBIDDEN, desc = "hit exception during invoke")
    public static class SynchronizedInstantArrayRateLimiterInvokeTest {

        @Actor
        public void actor1(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor2(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor3(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor4(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor5(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor6(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor7(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void actor8(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            invoke(wrapper, result);
        }

        @Actor
        public void checkActor(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            checkEmittedTimes(wrapper, result);
        }

        @State
        public static class SynchronizedInstantArrayRateLimiterWrapper extends RateLimiterWrapper<SynchronizedInstantArrayRateLimiter> {
            SynchronizedInstantArrayRateLimiterWrapper() {
                super(new SynchronizedInstantArrayRateLimiter(MAX_INVOKES, DURATION), 8);
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

        @State
        public static class SynchronizedLongArrayRateLimiterWrapper extends RateLimiterWrapper<SynchronizedLongArrayRateLimiter> {
            SynchronizedLongArrayRateLimiterWrapper() {
                super(new SynchronizedLongArrayRateLimiter(MAX_INVOKES, DURATION), 8);
            }
        }
    }
}


