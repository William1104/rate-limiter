# Rate Limiter

A simple Rate Limiter implementation. We define a rate as _N_ invocation in the last _T_ duration. That means a rate
limiter cannot be invoked more than _N_ times in _T_ duration. If more than _N_ invocation already happened in last _T'_
duration, a rate limiter will block the current execution for _T - T'_.

# Usage

Following example showing how to limit to call ```SomeService.invoke(...)``` at most 1000 times in 1 second.

```java
import java.util.concurrent.TimeUnit;

import one.williamwong.ratelimiter.*;

class Main {
    private static Random random = new Random(System.nanoTime());

    public static void main(String[] args) {
        // create a rate limiter
        final RateLimiter rateLimiter = new SynchronizedRateLimiter(1000, Duration.ofSecons(1));

        for (int i = 0; i < 100_000; i++) {
            // sleep for 0 - 1000 nanoseconds.
            TimeUnit.NANOSECONDS.sleep(random.nextInt(1_000));
            rateLimiter.invoke();

            // do whatever should be rate limited.
            SomeService.invoke(....);
        }
    }
}
```

We can limit invocation rate with time-scale less than one second. For example, we can limit to call
```SomeService.invoke(...)``` at most 1000 times in 1 microsecond with following example.

```java
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import one.williamwong.ratelimiter.*;

class Main {
    private static Random random = new Random(System.nanoTime());

    public static void main(String[] args) {
        // create a rate limiter
        final RateLimiter rateLimiter = new SynchronizedRateLimiter(1000, Duration.ofNanos(1000));

        for (int i = 0; i < 100_000; i++) {
            rateLimiter.invoke();

            // do whatever should be rate limited.
            SomeService.invoke(....);
        }
    }
}
```

We also can limit invocation rate with huge invocation limit. However, it also means hug amount of memory will be used
for storing the invocation history.

To reduce the memory usage, we can raise the _sampleInterval_ (default to 1). Rate limiter only remembers the invocation
time of 1st, N+1 th, 2N+1 th, 3N+1 th ... The greater sample interval, the less memory will be required, but also less
accurate in terms of the projected invocation count.

For example, we can limit calling ```SomeService.invoke(...)``` at most 1_000_000_000 times in 1 hour with sampling
interval 1_000_000 requests with following example. The rate limiter will keep only 1_000_000_000 / 1_000_000 = 1_000
invocation records.

```java
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import one.williamwong.ratelimiter.*;

class Main {
    private static Random random = new Random(System.nanoTime());

    public static void main(String[] args) {
        // create a rate limiter
        final RateLimiter rateLimiter = new SynchronizedRateLimiter(1_000_000_000, Duration.ofHours(1), 1_000_000);

        for (int i = 0; i < 100_000; i++) {
            rateLimiter.invoke();

            // do whatever should be rate limited.
            SomeService.invoke(....);
        }
    }
}
```