package one.williamwong.ratelimiter;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import java.time.Duration;
import java.time.Instant;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.FORBIDDEN;

class RateLimiterWrapper<T extends IRateLimiter> {
    private final T rateLimiter;

    RateLimiterWrapper(Class<T> rateLimiterClass, int limit, Duration duration) {
        try {
            this.rateLimiter = rateLimiterClass
                    .getDeclaredConstructor(ISleeper.class, int.class, Duration.class)
                    .newInstance(new Sleeper(), limit, duration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public T getRateLimiter() {
        return rateLimiter;
    }
}

public class RateLimiterThreadSafetyTest {
    private static void actorAcquire(IRateLimiter rateLimiter, L_Result result) {
        for (int i = 0; i < 100; i++) {
            try {
                rateLimiter.acquire();
            } catch (InterruptedException e) {
                result.r1 = e;
            }
        }
    }

    private static void checkIfRecordsInOrder(IRateLimiter rateLimiter) throws Exception {
        var pointerField = rateLimiter.getClass().getDeclaredField("pointer");
        pointerField.setAccessible(true);
        var pointer = (int) pointerField.get(rateLimiter);

        var recordField = rateLimiter.getClass().getDeclaredField("records");
        recordField.setAccessible(true);
        var records = recordField.get(rateLimiter);

        if (records instanceof Instant[]) {
            final Instant[] instantRecords = (Instant[]) records;
            for (int i = 0; i < instantRecords.length - 1; i++) {
                int currPosition = (pointer + i) % instantRecords.length;
                int nextPosition = (pointer + i + 1) % instantRecords.length;
                var curr = instantRecords[currPosition];
                var next = instantRecords[nextPosition];
                if (curr != null && next != null && curr.isAfter(next)) {
                    throw new IllegalStateException("out of order records are detected");
                }
            }
        } else if (records instanceof long[]) {
            final long[] nanoRecords = (long[]) records;
            for (int i = 0; i < nanoRecords.length - 1; i++) {
                int currPosition = (pointer + i) % nanoRecords.length;
                int nextPosition = (pointer + i + 1) % nanoRecords.length;
                var curr = nanoRecords[currPosition];
                var next = nanoRecords[nextPosition];
                if (curr != 0 && next != 0 && curr > next) {
                    throw new IllegalStateException("out of order records are detected");
                }
            }
        } else {
            throw new IllegalStateException("records is neither Instant[] or long[]");
        }
    }

    @JCStressTest
    @Description("Test race on concurrent acquire")
    @Outcome(id = "null", expect = ACCEPTABLE, desc = "all records are increasing")
    @Outcome(id = ".*", expect = FORBIDDEN, desc = "hit exception during acquire")
    public static class StampLockInstantArrayRateLimiterAcquireTest {

        @Actor
        public void actor1(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor2(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor3(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor4(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor5(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor6(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor7(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor8(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Arbiter
        public void checkActor(StampLockInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            var rateLimiter = wrapper.getRateLimiter();
            try {
                checkIfRecordsInOrder(rateLimiter);
            } catch (Exception e) {
                result.r1 = e;
            }
        }

        @State
        public static class StampLockInstantArrayRateLimiterWrapper extends RateLimiterWrapper<StampLockInstantArrayRateLimiter> {
            StampLockInstantArrayRateLimiterWrapper() {
                super(StampLockInstantArrayRateLimiter.class, 100, Duration.ofNanos(1));
            }
        }
    }

    @JCStressTest
    @Description("Test race on concurrent acquire")
    @Outcome(id = "null", expect = ACCEPTABLE, desc = "all records are increasing")
    @Outcome(id = ".*", expect = FORBIDDEN, desc = "hit exception during acquire")
    public static class StampLockLongArrayRateLimiterAcquireTest {

        @Actor
        public void actor1(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor2(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor3(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor4(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor5(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor6(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor7(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor8(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Arbiter
        public void checkActor(StampLockLongArrayRateLimiterWrapper wrapper, L_Result result) {
            var rateLimiter = wrapper.getRateLimiter();
            try {
                checkIfRecordsInOrder(rateLimiter);
            } catch (Exception e) {
                result.r1 = e;
            }
        }

        @State
        public static class StampLockLongArrayRateLimiterWrapper extends RateLimiterWrapper<StampLockLongArrayRateLimiter> {
            StampLockLongArrayRateLimiterWrapper() {
                super(StampLockLongArrayRateLimiter.class, 100, Duration.ofNanos(1));
            }
        }
    }

    @JCStressTest
    @Description("Test race on concurrent acquire")
    @Outcome(id = "null", expect = ACCEPTABLE, desc = "all records are increasing")
    @Outcome(id = ".*", expect = FORBIDDEN, desc = "hit exception during acquire")
    public static class SynchronizedInstantArrayRateLimiterAcquireTest {

        @Actor
        public void actor1(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor2(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor3(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor4(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor5(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor6(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor7(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor8(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Arbiter
        public void checkActor(SynchronizedInstantArrayRateLimiterWrapper wrapper, L_Result result) {
            var rateLimiter = wrapper.getRateLimiter();
            try {
                checkIfRecordsInOrder(rateLimiter);
            } catch (Exception e) {
                result.r1 = e;
            }
        }

        @State
        public static class SynchronizedInstantArrayRateLimiterWrapper extends RateLimiterWrapper<SynchronizedInstantArrayRateLimiter> {
            SynchronizedInstantArrayRateLimiterWrapper() {
                super(SynchronizedInstantArrayRateLimiter.class, 100, Duration.ofNanos(1));
            }
        }
    }

    @JCStressTest
    @Description("Test race on concurrent acquire")
    @Outcome(id = "null", expect = ACCEPTABLE, desc = "all records are increasing")
    @Outcome(id = ".*", expect = FORBIDDEN, desc = "hit exception during acquire")
    public static class SynchronizedLongArrayRateLimiterAcquireTest {

        @Actor
        public void actor1(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor2(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor3(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor4(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor5(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor6(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor7(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Actor
        public void actor8(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            actorAcquire(wrapper.getRateLimiter(), result);
        }

        @Arbiter
        public void checkActor(SynchronizedLongArrayRateLimiterWrapper wrapper, L_Result result) {
            var rateLimiter = wrapper.getRateLimiter();
            try {
                checkIfRecordsInOrder(rateLimiter);
            } catch (Exception e) {
                result.r1 = e;
            }
        }

        @State
        public static class SynchronizedLongArrayRateLimiterWrapper extends RateLimiterWrapper<SynchronizedLongArrayRateLimiter> {
            SynchronizedLongArrayRateLimiterWrapper() {
                super(SynchronizedLongArrayRateLimiter.class, 100, Duration.ofNanos(1));
            }
        }
    }
}


