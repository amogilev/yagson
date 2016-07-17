package am.yagson;

import junit.framework.TestCase;

import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextSupport;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import static am.yagson.TestingUtils.MY_STRING_CMP;
import static am.yagson.TestingUtils.jsonStr;

public class TestVariousLists extends TestCase {
    // TODO: test lists (separate test file?): checkedList(), emptyList(), list(), singletonList(), nCopies(),
    //   synchronizedList

    public void testUnmodifiableList() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        List<Long> obj = Collections.unmodifiableList(l);
        TestingUtils.testFully(obj, jsonStr("{'list':[1],'c':'@.list'}"));
    }

}
