package ru.edwgiz.test.scheduling;


import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Single-threaded executor with a specific prioritization. The task will be the most first executed when
 * <ol>
 * <li>Schedule time is before than others</li>
 * <li>Receiving time is before than others</li>
 * <li>Task counter is the lowest</li>
 * </ol>
 */
final class ScheduleService extends ScheduledThreadPoolExecutor implements IScheduleService {

    public ScheduleService() {
        super(1);
    }


    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
        final CallableDecorator<V> cd = (CallableDecorator<V>) callable;
        return new FutureDecorator<>(cd.getSchedulingTimeMillis(), cd.getReceivingMillis(), task);
    }


    @Override
    public <V> ScheduledFuture<V> schedule(Date dateTime, Callable<V> callable) {
        return schedule(dateTime, callable, currentTimeMillis());
    }

    /**
     * Method can be used directly only at testing time
     *
     * @param dateTime the time to delay execution
     * @param callable the function to execute
     * @param now      current time millis
     * @param <V>      return value type
     * @return a ScheduledFuture that can be used to extract result or cancel
     */
    <V> ScheduledFuture<V> schedule(Date dateTime, Callable<V> callable, long now) {
        final long schedulingTimeMillis = dateTime.getTime();
        long delay = schedulingTimeMillis - now;
        if (delay < 0) {
            delay = 0;
        }
        final CallableDecorator<V> callableDecorator = new CallableDecorator<>(schedulingTimeMillis, now, callable);
        return schedule(callableDecorator, delay, MILLISECONDS);
    }

}
