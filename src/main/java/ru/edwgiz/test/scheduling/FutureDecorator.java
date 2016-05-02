package ru.edwgiz.test.scheduling;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Long.MIN_VALUE;

/**
 * Decorates {@link RunnableScheduledFuture} to realize a specific sorting in
 * {@link java.util.concurrent.ScheduledThreadPoolExecutor.DelayedWorkQueue}
 *
 * @param <V> returned value type
 */
final class FutureDecorator<V> implements RunnableScheduledFuture<V> {

    private final static AtomicLong COUNT = new AtomicLong(MIN_VALUE);

    private final long schedulingTimeMillis;
    private final long receivingMillis;
    private final long count;
    private final RunnableScheduledFuture<V> underlyingTask;

    FutureDecorator(long schedulingTimeMillis, long receivingMillis, RunnableScheduledFuture<V> underlyingTask) {
        this.schedulingTimeMillis = schedulingTimeMillis;
        this.receivingMillis = receivingMillis;
        this.count = COUNT.incrementAndGet();
        this.underlyingTask = underlyingTask;
    }

    @Override
    public boolean isPeriodic() {
        return underlyingTask.isPeriodic();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return underlyingTask.getDelay(unit);
    }

    @Override
    public int compareTo(Delayed o) {
        final FutureDecorator fd = (FutureDecorator) o;
        int result = Long.compare(schedulingTimeMillis, fd.schedulingTimeMillis);
        if (result == 0) {
            result = Long.compare(receivingMillis, fd.receivingMillis);
            if (result == 0) {
                result = Long.compare(count, fd.count);
            }
        }
        return result;
    }

    @Override
    public void run() {
        underlyingTask.run();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return underlyingTask.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return underlyingTask.isCancelled();
    }

    @Override
    public boolean isDone() {
        return underlyingTask.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return underlyingTask.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return underlyingTask.get(timeout, unit);
    }
}
