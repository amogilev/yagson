package am.yagson;

import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static am.yagson.TestingUtils.jsonStr;

public class TestThreads extends TestCase {

    private static ThreadGroup getSystemThreadGroup() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        while (tg.getParent() != null) {
            tg = tg.getParent();
        }
        return tg;
    }

    public void testThreadGroups() {
        ThreadGroup curGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup systemGroup = getSystemThreadGroup();

        TestingUtils.testFully(systemGroup, jsonStr("'system'"));
        TestingUtils.testFully(curGroup, jsonStr("'system.main'"));
    }

    public void testThreadGroupWithEscapedNames() {
        ThreadGroup systemGroup = getSystemThreadGroup();
        ThreadGroup myGroup = new ThreadGroup(systemGroup, "test1.test2..test3_.__");

        TestingUtils.testFully(myGroup, jsonStr("'system.test1_.test2_._.test3___.____'"));
    }

    public void testCurrentThread() {
        Thread currentThread = Thread.currentThread();

        TestingUtils.testFully(currentThread, jsonStr("'system.main.main'"));
    }

    public void testUnstartedThreadIsMissing() {
        // unstarted threads are not registered in thread groups and cannot be found
        Thread obj = new Thread("foo1");
        TestingUtils.testWithNullExpected(obj, Thread.class, jsonStr("'system.main.foo1'"));
    }

    public void testStartedCustomThread() {
        // latch is used to keep the custom thread running until the end of the test
        final CountDownLatch latch = new CountDownLatch(1);
        // unstarted threads are not registered in thread groups and cannot be found
        Thread obj = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "foo.2");

        obj.start();
        TestingUtils.testFully(obj, Thread.class, jsonStr("'system.main.foo_.2'"));
        latch.countDown();
    }

    private static AtomicLong cnt = new AtomicLong(1);
    private static ThreadLocal<Long> threadLocalId = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
            return cnt.getAndIncrement();
        }
    };

    public void testThreadLocalInSameThread() {
        long cntBeforeTest = cnt.get();
        ThreadLocal<Long> readThreadLocal =
                TestingUtils.test(threadLocalId, new TypeToken<ThreadLocal<Long>>() {}, jsonStr(
                    "{'@type':'am.yagson.TestThreads$2','@val':{}}"));
        // make sure that the thread local was not initialized by the serialization
        long cntAfterTest = cnt.get();
        assertEquals("Uninitialized ThreadLocals shall not be initialized during serialization",
                cntBeforeTest, cntAfterTest);

        long curThreadLocalValue = readThreadLocal.get();
        assertEquals(cntAfterTest, curThreadLocalValue);

        ThreadLocal<Long> readThreadLocal2 = TestingUtils.test(readThreadLocal, jsonStr(
                "{'@.value':" + curThreadLocalValue + "}"));
        assertEquals(curThreadLocalValue, readThreadLocal2.get().longValue());
    }

    public void testThreadLocalInDifferentThreads() throws InterruptedException {
        final AtomicLong otherThreadId = new AtomicLong();
        final AtomicReference<String> jsonRef = new AtomicReference<String>();
        long thisThreadId = threadLocalId.get();

        final CountDownLatch latch1 = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                otherThreadId.set(threadLocalId.get());
                String json = new YaGson().toJson(threadLocalId, ThreadLocal.class);
                jsonRef.set(json);
                latch1.countDown();
            }
        }).start();
        assertTrue(latch1.await(10, TimeUnit.SECONDS));

        assertTrue(thisThreadId != otherThreadId.get());

        String json = jsonRef.get();
        assertEquals(jsonStr("{'@type':'am.yagson.TestThreads$2','@val':{'@.value':" + otherThreadId + "}}"), json);

        final ThreadLocal<Long> outThreadLocal = new YaGson().fromJson(json, new TypeToken<ThreadLocal<Long>>(){}.getType());
        assertEquals(otherThreadId.get(), outThreadLocal.get().longValue());

        // make sure that the new thread local is working, i.e. yield another value for another thread
        final CountDownLatch latch2 = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                otherThreadId.set(outThreadLocal.get());
                latch2.countDown();
            }
        }).start();
        assertTrue(latch2.await(10, TimeUnit.SECONDS));

        assertTrue("Expects different ThreadLocal value", otherThreadId.get() != outThreadLocal.get());
    }

    public void testInheritableThreadLocalInDifferentThreads() throws InterruptedException {
        final InheritableThreadLocal<Long> itl = new InheritableThreadLocal<Long>();
        itl.set(10L);

        final AtomicReference<String> jsonRef = new AtomicReference<String>();
        final CountDownLatch latch1 = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                assertEquals(10L, itl.get().longValue());
                itl.set(20L);

                String json = new YaGson().toJson(itl, ThreadLocal.class);
                jsonRef.set(json);
                latch1.countDown();
            }
        }).start();
        assertTrue(latch1.await(10, TimeUnit.SECONDS));


        String json = jsonRef.get();
        assertEquals(jsonStr("{'@type':'java.lang.InheritableThreadLocal','@val':{'@.value':20}}"), json);

        final ThreadLocal<Long> outThreadLocal = new YaGson().fromJson(json, new TypeToken<ThreadLocal<Long>>(){}.getType());
        assertEquals(InheritableThreadLocal.class, outThreadLocal.getClass());
        assertEquals(20L, outThreadLocal.get().longValue());

        // make sure that the new thread local is working
        final CountDownLatch latch2 = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                assertEquals(20L, outThreadLocal.get().longValue());
                outThreadLocal.set(30L);
                assertEquals(30L, outThreadLocal.get().longValue());
                latch2.countDown();
            }
        }).start();
        assertTrue(latch2.await(10, TimeUnit.SECONDS));

        assertEquals(20L, outThreadLocal.get().longValue());
    }
}
