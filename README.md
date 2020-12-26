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

With option:  -server, -XX:+UnlockDiagnosticVMOptions, -XX:+UseNUMA

|Benchmark                        |                   (rateLimiterType) |  Mode | Cnt |     Score |     Error |  Units |
|---------------------------------|-------------------------------------|-------|-----|-----------|-----------|--------|
|RaterLimiterBenchmark.thread_1   |       StampLockLongArrayRateLimiter | thrpt |  90 | 21487.385 |▒ 1082.163 | ops/ms |
|RaterLimiterBenchmark.thread_1   |    StampLockInstantArrayRateLimiter | thrpt |  90 | 13162.330 |▒ 1585.555 | ops/ms |
|RaterLimiterBenchmark.thread_1   |    SynchronizedLongArrayRateLimiter | thrpt |  90 | 15362.934 |▒  227.704 | ops/ms |
|RaterLimiterBenchmark.thread_1   | SynchronizedInstantArrayRateLimiter | thrpt |  90 | 17281.675 |▒ 2148.057 | ops/ms |
|RaterLimiterBenchmark.thread_10  |       StampLockLongArrayRateLimiter | thrpt |  90 |  6868.653 |▒  146.372 | ops/ms |
|RaterLimiterBenchmark.thread_10  |    StampLockInstantArrayRateLimiter | thrpt |  90 |  8189.747 |▒  335.517 | ops/ms |
|RaterLimiterBenchmark.thread_10  |    SynchronizedLongArrayRateLimiter | thrpt |  90 |  6643.004 |▒  103.568 | ops/ms |
|RaterLimiterBenchmark.thread_10  | SynchronizedInstantArrayRateLimiter | thrpt |  90 |  5252.975 |▒  190.363 | ops/ms |
|RaterLimiterBenchmark.thread_100 |       StampLockLongArrayRateLimiter | thrpt |  90 |  7352.890 |▒ 2109.446 | ops/ms |
|RaterLimiterBenchmark.thread_100 |    StampLockInstantArrayRateLimiter | thrpt |  90 |  8675.814 |▒  922.653 | ops/ms |
|RaterLimiterBenchmark.thread_100 |    SynchronizedLongArrayRateLimiter | thrpt |  90 |  6509.368 |▒  157.212 | ops/ms |
|RaterLimiterBenchmark.thread_100 | SynchronizedInstantArrayRateLimiter | thrpt |  90 |  5042.867 |▒  192.971 | ops/ms |

With option:  -server, -XX:+UnlockDiagnosticVMOptions, -XX:+UseNUMA, -XX:+UseLWPSynchronization

|Benchmark                        |                   (rateLimiterType) |  Mode | Cnt |     Score |     Error |  Units |
|---------------------------------|-------------------------------------|-------|-----|-----------|-----------|--------|
|RaterLimiterBenchmark.thread_1   |       StampLockLongArrayRateLimiter | thrpt |  90 | 11383.198 |▒  353.921 | ops/ms |
|RaterLimiterBenchmark.thread_1   |    StampLockInstantArrayRateLimiter | thrpt |  90 | 11666.918 |▒  842.426 | ops/ms |
|RaterLimiterBenchmark.thread_1   |    SynchronizedLongArrayRateLimiter | thrpt |  90 | 15696.852 |▒  371.078 | ops/ms |
|RaterLimiterBenchmark.thread_1   | SynchronizedInstantArrayRateLimiter | thrpt |  90 | 15357.617 |▒  650.846 | ops/ms |
|RaterLimiterBenchmark.thread_10  |       StampLockLongArrayRateLimiter | thrpt |  90 |  6937.050 |▒  130.727 | ops/ms |
|RaterLimiterBenchmark.thread_10  |    StampLockInstantArrayRateLimiter | thrpt |  90 |  8268.909 |▒  291.471 | ops/ms |
|RaterLimiterBenchmark.thread_10  |    SynchronizedLongArrayRateLimiter | thrpt |  90 |  9134.319 |▒ 1208.998 | ops/ms |
|RaterLimiterBenchmark.thread_10  | SynchronizedInstantArrayRateLimiter | thrpt |  90 |  5294.341 |▒  225.995 | ops/ms |
|RaterLimiterBenchmark.thread_100 |       StampLockLongArrayRateLimiter | thrpt |  90 |  8453.825 |▒ 1075.312 | ops/ms |
|RaterLimiterBenchmark.thread_100 |    StampLockInstantArrayRateLimiter | thrpt |  90 | 16297.921 |▒  611.255 | ops/ms |
|RaterLimiterBenchmark.thread_100 |    SynchronizedLongArrayRateLimiter | thrpt |  90 | 12536.378 |▒  974.951 | ops/ms |
|RaterLimiterBenchmark.thread_100 | SynchronizedInstantArrayRateLimiter | thrpt |  90 |  9051.560 |▒ 1303.856 | ops/ms |
