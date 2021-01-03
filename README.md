# rate-limiter
A simple Rate Limiter implementation. It is capable to limit speed with interval less than a second. (eg no more than 1000 requests in 100us)

Currently, there are two implementations.  
- StampLockRateLimiter 
- SynchronizedRateLimiter

# How to build the project with gradle
> gradle build

# How to run JMH with gradle
> gradle jmh

# How to run JCSTRESS with gradle
> gradle jcstress

# A sample JMH result 
On a laptop with CPU ( Intel(R) Core(TM) i5-1035G4 CPU @ 1.10GHz) with 8G physical memory.

With option:  -server, -XX:+UnlockDiagnosticVMOptions, -XX:+UseNUMA, -XX:-UseLWPSynchronization

|Benchmark                        |       (rateLimiterType) |  Mode | Cnt |     Score |     Error |  Units |
|---------------------------------|-------------------------|-------|-----|-----------|-----------|--------|
|RaterLimiterBenchmark.thread_1   |        GuavaRateLimiter | thrpt |  15 |  6567.358 |▒  135.104 | ops/ms |
|RaterLimiterBenchmark.thread_1   |    StampLockRateLimiter | thrpt |  15 | 10612.681 |▒  207.762 | ops/ms |
|RaterLimiterBenchmark.thread_1   | SynchronizedRateLimiter | thrpt |  15 | 14597.511 |▒  435.532 | ops/ms |
|RaterLimiterBenchmark.thread_10  |        GuavaRateLimiter | thrpt |  15 |  4733.286 |▒ 1874.901 | ops/ms |
|RaterLimiterBenchmark.thread_10  |    StampLockRateLimiter | thrpt |  15 | 17003.344 |▒  786.402 | ops/ms |
|RaterLimiterBenchmark.thread_10  | SynchronizedRateLimiter | thrpt |  15 | 13178.993 |▒  690.287 | ops/ms |
|RaterLimiterBenchmark.thread_100 |        GuavaRateLimiter | thrpt |  15 |  6484.510 |▒  402.729 | ops/ms |
|RaterLimiterBenchmark.thread_100 |    StampLockRateLimiter | thrpt |  15 | 17459.163 |▒  602.203 | ops/ms |
|RaterLimiterBenchmark.thread_100 | SynchronizedRateLimiter | thrpt |  15 | 11796.656 |▒ 2558.414 | ops/ms |
