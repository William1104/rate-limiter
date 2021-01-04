package one.williamwong.ratelimiter;

import java.time.Duration;
import java.util.concurrent.locks.StampedLock;

public class StampLockRateLimiter extends AbstractRateLimiter {

    private final StampedLock lock;


    public StampLockRateLimiter(int maxInvokes, Duration duration) {
        super(maxInvokes, duration, 1);
        this.lock = new StampedLock();
    }

    public StampLockRateLimiter(int maxInvokes, Duration duration, int density) {
        super(maxInvokes, duration, density);
        this.lock = new StampedLock();
    }


    @Override public long invoke() throws InterruptedException {
        final long stamp = lock.writeLock();
        try {
            return record(pauseIfRequired());
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override public void reset() {
        final long stamp = lock.writeLock();
        try {
            resetHistory();
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
