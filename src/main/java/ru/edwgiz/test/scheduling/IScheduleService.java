package ru.edwgiz.test.scheduling;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

public interface IScheduleService {

    /**
     * Creates and executes a ScheduledFuture that becomes enabled at the given {@code dateTime}
     *
     * @param dateTime the time to delay execution
     * @param callable the function to execute
     * @param <V>      return value type
     * @return a ScheduledFuture that can be used to extract result or cancel
     */
    <V> ScheduledFuture<V> schedule(Date dateTime, Callable<V> callable);

}

