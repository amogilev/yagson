package am.yagson;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

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
}
