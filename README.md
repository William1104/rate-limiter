# rate-limiter
A simple Rate Limiter implementation. It is capable to limit speed with interval less than a second. (eg no more than 1000 requests in 100ms)

Currently, we have 4 implementations.  
- StampLockInstantArrayRateLimiter
- StampLockLongArrayRateLimiter 
- SynchronizedInstantArrayRateLimiter
- SynchronizedLongArrayRateLimiter

# How to build the project with gradle
> gradle build

# How to run JMH with gradle
> gradle jmh

# How to run JCSTRESS with gradle
> gradle jcstress

# A sample JMH result 
On a laptop with CPU ( Intel(R) Core(TM) i5-1035G4 CPU @ 1.10GHz) with 8G physical memory.

With option:  -server, -XX:+UnlockDiagnosticVMOptions, -XX:+UseNUMA, -XX:-UseLWPSynchronization

|Benchmark                        |                   (rateLimiterType) |  Mode | Cnt |     Score |     Error |  Units |
|---------------------------------|-------------------------------------|-------|-----|-----------|-----------|--------|
|RaterLimiterBenchmark.thread_1   |       StampLockLongArrayRateLimiter | thrpt |  90 | 23573.282 |▒  364.739 | ops/ms |
|RaterLimiterBenchmark.thread_1   |    StampLockInstantArrayRateLimiter | thrpt |  90 | 23062.260 |▒ 1035.395 | ops/ms |
|RaterLimiterBenchmark.thread_1   |    SynchronizedLongArrayRateLimiter | thrpt |  90 | 34667.411 |▒  246.003 | ops/ms |
|RaterLimiterBenchmark.thread_1   | SynchronizedInstantArrayRateLimiter | thrpt |  90 | 36426.369 |▒ 1248.360 | ops/ms |
|RaterLimiterBenchmark.thread_10  |       StampLockLongArrayRateLimiter | thrpt |  90 | 13592.158 |▒   76.319 | ops/ms |
|RaterLimiterBenchmark.thread_10  |    StampLockInstantArrayRateLimiter | thrpt |  90 | 14564.306 |▒  474.613 | ops/ms |
|RaterLimiterBenchmark.thread_10  |    SynchronizedLongArrayRateLimiter | thrpt |  90 | 13524.610 |▒  155.850 | ops/ms |
|RaterLimiterBenchmark.thread_10  | SynchronizedInstantArrayRateLimiter | thrpt |  90 | 13080.967 |▒  309.736 | ops/ms |
|RaterLimiterBenchmark.thread_100 |       StampLockLongArrayRateLimiter | thrpt |  90 | 13224.529 |▒  459.035 | ops/ms |
|RaterLimiterBenchmark.thread_100 |    StampLockInstantArrayRateLimiter | thrpt |  90 | 13890.278 |▒  456.182 | ops/ms |
|RaterLimiterBenchmark.thread_100 |    SynchronizedLongArrayRateLimiter | thrpt |  90 | 12672.925 |▒  314.118 | ops/ms |
|RaterLimiterBenchmark.thread_100 | SynchronizedInstantArrayRateLimiter | thrpt |  90 | 12245.120 |▒  296.395 | ops/ms |
