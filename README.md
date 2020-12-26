# rate-limiter
A simple Rate Limiter implementation. It is a testbed for me to familiar how to measurement 
a program's performance with JMH.

Currently, we have 4 implementations: 
- StampLockInstantArrayRateLimiter
- StampLockLongArrayRateLimiter 
- SynchronizedInstantArrayRateLimiter
- SynchronizedLongArrayRateLimiter

So far, StampLockInstantArrayRateLimiter performs the best. 

# How to build the project with gradle
> gradle build

# How to run JMH with gradle
> gradle jmh

# The sample JMH result 
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

With option:  -server, -XX:+UnlockDiagnosticVMOptions, -XX:+UseNUMA, -XX:+UseLWPSynchronization

|Benchmark                        |                   (rateLimiterType) |  Mode | Cnt |     Score |     Error |  Units |
|---------------------------------|-------------------------------------|-------|-----|-----------|-----------|--------|
|RaterLimiterBenchmark.thread_1   |       StampLockLongArrayRateLimiter | thrpt |  90 | 24842.514 |▒ 372.521  | ops/ms |
|RaterLimiterBenchmark.thread_1   |    StampLockInstantArrayRateLimiter | thrpt |  90 | 24327.864 |▒ 322.659  | ops/ms |
|RaterLimiterBenchmark.thread_1   |    SynchronizedLongArrayRateLimiter | thrpt |  90 | 34490.411 |▒ 330.288  | ops/ms |
|RaterLimiterBenchmark.thread_1   | SynchronizedInstantArrayRateLimiter | thrpt |  90 | 38383.257 |▒ 654.269  | ops/ms |
|RaterLimiterBenchmark.thread_10  |       StampLockLongArrayRateLimiter | thrpt |  90 | 13536.284 |▒  74.613  | ops/ms |
|RaterLimiterBenchmark.thread_10  |    StampLockInstantArrayRateLimiter | thrpt |  90 | 13702.022 |▒ 289.616  | ops/ms |
|RaterLimiterBenchmark.thread_10  |    SynchronizedLongArrayRateLimiter | thrpt |  90 | 12530.107 |▒ 243.471  | ops/ms |
|RaterLimiterBenchmark.thread_10  | SynchronizedInstantArrayRateLimiter | thrpt |  90 | 10795.833 |▒ 158.400  | ops/ms |
|RaterLimiterBenchmark.thread_100 |       StampLockLongArrayRateLimiter | thrpt |  90 | 13204.275 |▒ 200.937  | ops/ms |
|RaterLimiterBenchmark.thread_100 |    StampLockInstantArrayRateLimiter | thrpt |  90 | 11606.823 |▒ 224.213  | ops/ms |
|RaterLimiterBenchmark.thread_100 |    SynchronizedLongArrayRateLimiter | thrpt |  90 | 11504.124 |▒ 107.543  | ops/ms |
|RaterLimiterBenchmark.thread_100 | SynchronizedInstantArrayRateLimiter | thrpt |  90 | 10732.451 |▒ 118.753  | ops/ms |
