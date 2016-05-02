package ru.edwgiz.test.scheduling;

import org.junit.Test;

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.assertEquals;


public class ScheduleServiceTest {

    @Test
    public void testReorderingByDateTime() throws Exception {
        TestCallable.EXEC_COUNT.set(0);

        final ScheduleService s = new ScheduleService();
        final long millisOffset = currentTimeMillis();
        final TestCallable callable0 = new TestCallable(new Date(millisOffset + 100L), 0);
        final TestCallable callable1 = new TestCallable(new Date(millisOffset + 200L), 1);

        s.schedule(callable1.dateTime, callable1);
        s.schedule(callable0.dateTime, callable0);

        s.shutdown();
        s.awaitTermination(1, MINUTES);

        assertEquals(0, callable0.actualExecCount);
        assertEquals(1, callable1.actualExecCount);
    }

    /**
     * Concurrently tests that {@link ru.edwgiz.test.scheduling.ScheduleService#schedule(java.util.Date, java.util.concurrent.Callable, long)}
     * calls the given callbacks in valid order.
     *
     * @throws Exception
     */
    @Test(timeout = 60000L)
    public void testConcurrently() throws Exception {
        TestCallable.EXEC_COUNT.set(0);

        final int putThreadCount = 10;
        final int putsPerThread = 500;
        final int n = putThreadCount * putsPerThread;

        // prepare callbacks
        final ArrayBlockingQueue<TestCallable> callables = new ArrayBlockingQueue<>(n);
        final long millisOffset = currentTimeMillis();
        for (int i = 0; i < n; i++) {
            final Date dateTime = new Date(millisOffset + (i / (50)) * (50));
            callables.add(new TestCallable(dateTime, i));
        }

        // schedule callbacks
        final ArrayBlockingQueue<Future<Void>> futures = new ArrayBlockingQueue<>(n);
        final ScheduleService s = new ScheduleService();
        final ExecutorService executorService = Executors.newFixedThreadPool(putThreadCount);
        for (int i = 0; i < putThreadCount; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    for (; ; ) {
                        final TestCallable callable = callables.poll();
                        if (callable == null) {
                            break;
                        }
                        final ScheduledFuture<Void> future = s.schedule(callable.dateTime, callable, callable.getReceivedMillis());
                        futures.add(future);
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, MINUTES);

        // wait until all calls are completed
        for (Future<Void> future : futures) {
            future.get();
        }
        s.shutdown();
        s.awaitTermination(1, MINUTES);

        // check execution order
        for (TestCallable callable : callables) {
            assertEquals(callable.expectedExecCount, callable.actualExecCount);
        }
    }


    private static class TestCallable implements Callable<Void> {

        private static final AtomicInteger EXEC_COUNT = new AtomicInteger();

        private final Date dateTime;
        private final int expectedExecCount;
        private volatile int actualExecCount;
        private volatile long receivedMillis;

        TestCallable(Date dateTime, int expectedExecCount) {
            this.dateTime = dateTime;
            this.expectedExecCount = expectedExecCount;
        }

        /**
         * Initializes and gets received time
         *
         * @return millis
         */
        long getReceivedMillis() {
            if (receivedMillis == 0) {
                receivedMillis = currentTimeMillis();
            }
            return receivedMillis;
        }

        /**
         * Counts call order of callbacks of this class
         *
         * @return void
         */
        @Override
        public Void call() {
            actualExecCount = EXEC_COUNT.getAndIncrement();
            System.out.println(toString());
            return null;
        }

        @Override
        public String toString() {
            return "TestCallable{" +
                    "dateTime=" + dateTime +
                    ", receivedMillis=" + receivedMillis +
                    ", expectedExecCount=" + expectedExecCount +
                    ", actualExecCount=" + actualExecCount +
                    '}';
        }
    }

}