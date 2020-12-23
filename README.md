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

|Benchmark                        |                   (rateLimiterType) |  Mode | Cnt |  Score |  Error  |  Units |
|---------------------------------|-------------------------------------|-------|-----|--------|---------|--------|
|RaterLimiterBenchmark.thread_1   |    StampLockInstantArrayRateLimiter | thrpt |  15 | 23.546 | ▒ 1.378 | ops/us |
|RaterLimiterBenchmark.thread_1   |       StampLockLongArrayRateLimiter | thrpt |  15 | 51.569 | ▒ 1.235 | ops/us |
|RaterLimiterBenchmark.thread_1   | SynchronizedInstantArrayRateLimiter | thrpt |  15 | 37.822 | ▒ 1.583 | ops/us |
|RaterLimiterBenchmark.thread_10  |    StampLockInstantArrayRateLimiter | thrpt |  15 | 15.227 | ▒ 1.925 | ops/us |
|RaterLimiterBenchmark.thread_10  |       StampLockLongArrayRateLimiter | thrpt |  15 | 33.281 | ▒ 0.536 | ops/us |
|RaterLimiterBenchmark.thread_10  | SynchronizedInstantArrayRateLimiter | thrpt |  15 |  7.038 | ▒ 0.175 | ops/us |
|RaterLimiterBenchmark.thread_10  |    SynchronizedLongArrayRateLimiter | thrpt |  15 | 15.147 | ▒ 0.532 | ops/us |
|RaterLimiterBenchmark.thread_100 |    StampLockInstantArrayRateLimiter | thrpt |  15 | 13.823 | ▒ 0.753 | ops/us |
|RaterLimiterBenchmark.thread_100 |       StampLockLongArrayRateLimiter | thrpt |  15 | 30.399 | ▒ 1.625 | ops/us |
|RaterLimiterBenchmark.thread_100 | SynchronizedInstantArrayRateLimiter | thrpt |  15 |  6.860 | ▒ 0.163 | ops/us |
|RaterLimiterBenchmark.thread_100 |    SynchronizedLongArrayRateLimiter | thrpt |  15 | 14.992 | ▒ 0.452 | ops/us |
