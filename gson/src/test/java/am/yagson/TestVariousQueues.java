package am.yagson;

import am.yagson.refs.ReferencesPolicy;
import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextSupport;
import java.util.*;
import java.util.concurrent.*;

import static am.yagson.TestingUtils.MY_STRING_CMP;
import static am.yagson.TestingUtils.jsonStr;

public class TestVariousQueues extends TestCase {

    //
    // Tests various Queues
    //
    public void testArrayDeque() {
        Deque<Long> obj = new ArrayDeque<Long>();
        obj.add(1L);

        TestingUtils.testFully(obj, jsonStr("[1]"));
    }

    // NOTE: JSON for blocking queues is ugly, but it is required for keeping the correct blocking functionality

    public void testLinkedBlockingDeque() {
        Deque<Long> obj = new LinkedBlockingDeque<Long>();
        obj.add(1L);

        // FIXME: transient policy required!
//        TestingUtils.testFully(obj);
    }

    public void testArrayBlockingQueue() {
        ArrayBlockingQueue<Long> obj = new ArrayBlockingQueue<Long>(10);
        obj.add(1L);

//        obj = TestingUtils.testFully(obj, jsonStr("" +
//                "{'items':[1,null,null,null,null,null,null,null,null,null],'takeIndex':0,'putIndex':1,'count':1," +
//                "'lock':{'sync':{'@type':'java.util.concurrent.locks.ReentrantLock$NonfairSync','@val':{'state':0}}}," +
//                "'notEmpty':{'@type':'java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject'," +
//                "'@val':{'this$0':{'@type':'java.util.concurrent.locks.ReentrantLock$NonfairSync','@val':{'state':0}}}}," +
//                "'notFull':{'@type':'java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject','@val':" +
//                "{'this$0':{'@type':'java.util.concurrent.locks.ReentrantLock$NonfairSync','@val':{'state':0}}}}}"));
        obj = TestingUtils.testFully(obj);
        assertEquals(9, obj.remainingCapacity());
    }

    public void testSynchronousQueue() throws InterruptedException {
        SynchronousQueue<Long> obj = new SynchronousQueue<Long>(true);
        // FIXME: 'fair' is not kept, requires transient policy
        TestingUtils.testFully(obj, "{}");
    }

    public void testPriorityQueue() {
        PriorityQueue<String> obj = new PriorityQueue<String>();
        obj.add("foo");
        obj.add("bar");

        TestingUtils.testFully(obj,
                jsonStr("['bar','foo']"));
    }

    public void testPriorityQueueWithComparator() {
        PriorityQueue<String> obj = new PriorityQueue<String>(10, MY_STRING_CMP);
        obj.add("foo");
        obj.add("bar");

        TestingUtils.testFully(obj,
                jsonStr("['@.comparator:',{'@type':'am.yagson.TestingUtils$1','@val':{}},'bar','foo']"));
    }

    public void testPriorityBlockingQueue() {
        PriorityBlockingQueue<String> obj = new PriorityBlockingQueue<String>(10, MY_STRING_CMP);
        obj.add("foo");
        obj.add("bar");

        TestingUtils.testFully(obj);
    }

    public void testConcurrentLinkedQueue() {
        Queue<String> obj = new ConcurrentLinkedQueue<String>();
        obj.add("foo");
        obj.add("bar");

        TestingUtils.testFully(obj, jsonStr("['foo','bar']"));
    }

    public void testDelayQueue() {
        class DelayedString implements Delayed {
            String str;
            int delaySec;

            public DelayedString(String str, int delaySec) {
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

        TestingUtils.testFully(obj, new TypeToken<DelayQueue<DelayedString>>(){},
                jsonStr("{'q':[{'str':'foo','delaySec':10,'this$0':{'fName':'testDelayQueue'}}]}"));
    }

    public void testAsLifoQueue() {
        Deque<Long> dq = new ArrayDeque<Long>();
        Queue<Long> obj = Collections.asLifoQueue(dq);
        obj.add(1L);

        TestingUtils.testFully(obj, jsonStr("{'q':{'@type':'java.util.ArrayDeque','@val':[1]}}"));
    }

    //
    // Tests for other non-Set non-Queue non-List Collections
    //
    public void testBeanContextSupport() throws PropertyVetoException {
        BeanContextSupport context = new BeanContextSupport();
        context.setLocale(Locale.CHINESE);

        BeanContextChildSupport bean = new BeanContextChildSupport();
        context.add(bean);

        // FIXME: requires advanced transient policies to work
//        context = TestingUtils.testFully(context);
//        assertEquals(1, context.size());
//        assertEquals(Locale.CHINESE, context.getLocale());
    }

    // TODO: test lists (separate test file?): checkedList(), emptyList(), list(), singletonList(), nCopies(),
    //   synchronizedList, unmodifiableList
}
