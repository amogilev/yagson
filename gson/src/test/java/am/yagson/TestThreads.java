package am.yagson;

import junit.framework.TestCase;

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

}
