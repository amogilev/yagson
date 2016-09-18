package com.gilecode.yagson.tests;

import com.gilecode.yagson.tests.util.BindingTestCase;
import com.google.gson.reflect.TypeToken;

import java.util.*;
import java.util.concurrent.*;

/**
 * Tests serialization of various standard {@link Queue}s from the Java Collections Framework.
 *
 * @author Andrey Mogilev
 */
public class TestVariousQueues extends BindingTestCase {

    //
    // Tests various Queues
    //
    public void testArrayDeque() {
        Deque<Long> obj = new ArrayDeque<Long>();
        obj.add(1L);

        test(obj, jsonStr("[1]"));
        testAsCollection(ArrayDeque.class);
    }

    // NOTE: JSON for blocking queues is ugly, but it is required for keeping the correct blocking functionality

    public void testLinkedBlockingDeque() throws InterruptedException {
        LinkedBlockingDeque<Long> obj = new LinkedBlockingDeque<Long>(2);
        obj.add(1L);

        final LinkedBlockingDeque result = test(obj, jsonStr(
                "{'first':{'item':1}," +
                        "'last':'@.first'," +
                        "'count':1,'capacity':2," +
                        "'lock':{'sync':{'@type':'java.util.concurrent.locks.ReentrantLock$NonfairSync','@val':{'state':0}}}," +
                        "'notEmpty':{'@type':'java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject'," +
                        "'@val':{'this$0':'@root.lock.sync'}}," +
                        "'notFull':{'@type':'java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject'," +
                        "'@val':{'this$0':'@root.lock.sync'}}}"));

        checkBlockingFunctionality(result, 100L);

        testAsCollection(LinkedBlockingDeque.class);

    }
    public void testArrayBlockingQueue() throws Exception {
        ArrayBlockingQueue<Long> obj = new ArrayBlockingQueue<Long>(10);
        obj.add(1L);

        obj = test(obj);
        assertEquals(9, obj.remainingCapacity());
        checkBlockingFunctionality(obj, 100L);

        testAsCollection(new ArrayBlockingQueue<String>(10, true, collectionsTestsElements));
    }

    public void testSynchronousQueue() throws InterruptedException {
        SynchronousQueue<Long> obj = new SynchronousQueue<Long>(true);
        test(obj);

        // NOTE: general collection functionality is not available for SynchronousQueue, so skip testing it
    }

    public void testPriorityQueue() {
        PriorityQueue<String> obj = new PriorityQueue<String>();
        obj.add("foo");
        obj.add("bar");

        test(obj, jsonStr("['bar','foo']"));
        testAsCollection(PriorityQueue.class);
    }

    public void testPriorityQueueWithComparator() {
        PriorityQueue<String> obj = new PriorityQueue<String>(10, MY_STRING_CMP);
        obj.add("foo");
        obj.add("bar");

        test(obj, jsonStr(
                "['@.comparator:',{'@type':'com.gilecode.yagson.tests.util.BindingTestCase$1','@val':{}},'bar','foo']"));
    }

    public void testPriorityBlockingQueue() throws Exception {
        PriorityBlockingQueue<String> obj = new PriorityBlockingQueue<String>(10, MY_STRING_CMP);
        obj.add("foo");
        obj.add("bar");

        obj = test(obj);
        checkBlockingFunctionality(obj, "baz");

        testAsCollection(obj);
    }

    public void testConcurrentLinkedQueue() {
        Queue<String> obj = new ConcurrentLinkedQueue<String>();
        obj.add("foo");
        obj.add("bar");

        test(obj, jsonStr("['foo','bar']"));
        testAsCollection(ConcurrentLinkedQueue.class);
    }

    public void testDelayQueue() {
        class DelayedString implements Delayed {
            private String str;
            private int delaySec;

            private DelayedString(String str, int delaySec) {
                this.str = str;
                this.delaySec = delaySec;
            }

            @Override
            public long getDelay(TimeUnit unit) {
                return unit.convert(delaySec, TimeUnit.SECONDS);
            }

            @Override
            public int compareTo(Delayed o) {
                return delaySec - ((DelayedString)o).delaySec;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                DelayedString that = (DelayedString) o;

                if (delaySec != that.delaySec) return false;
                return str.equals(that.str);

            }

            @Override
            public int hashCode() {
                int result = str.hashCode();
                result = 31 * result + delaySec;
                return result;
            }
        }

        Queue<DelayedString> obj = new DelayQueue<DelayedString>();
        obj.add(new DelayedString("foo", 10));

        test(obj, new TypeToken<DelayQueue<DelayedString>>(){}.getType(), null);
    }

    public void testAsLifoQueue() {
        Deque<Long> dq = new ArrayDeque<Long>();
        Queue<Long> obj = Collections.asLifoQueue(dq);
        obj.add(1L);

        test(obj, jsonStr("{'q':{'@type':'java.util.ArrayDeque','@val':[1]}}"));

        // TODO: known issue with iterator over ArrayDeque, to be fixed in 0.2
        // testAsCollection(obj);
    }

    private <E> void checkBlockingFunctionality(final BlockingQueue<E> bq, E element) throws InterruptedException {
        bq.clear();

        final CountDownLatch latch = new CountDownLatch(1);
        final Object[] foundResult = new Object[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    foundResult[0] = bq.poll(1, TimeUnit.SECONDS);
                    latch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        bq.add(element);
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(element, foundResult[0]);
    }
}
