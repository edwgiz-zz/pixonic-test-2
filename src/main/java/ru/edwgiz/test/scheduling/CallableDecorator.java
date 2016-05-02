package ru.edwgiz.test.scheduling;

import java.util.concurrent.Callable;

/**
 * This callable required to pass {@link #schedulingTimeMillis} and {@link #receivingMillis} to the {@link FutureDecorator}
 *
 * @param <V> returned value type
 */
final class CallableDecorator<V> implements Callable<V> {

    private final long schedulingTimeMillis;
    private final long receivingMillis;
    private final Callable<V> underlyingCallable;

    CallableDecorator(long schedulingTimeMillis, long receivingMillis, Callable<V> underlyingCallable) {
        this.receivingMillis = receivingMillis;
        this.underlyingCallable = underlyingCallable;
        this.schedulingTimeMillis = schedulingTimeMillis;
    }

    long getSchedulingTimeMillis() {
        return schedulingTimeMillis;
    }

    long getReceivingMillis() {
        return receivingMillis;
    }

    @Override
    public V call() throws Exception {
        return underlyingCallable.call();
    }
}
