package am.yagson;

import junit.framework.TestCase;

import static am.yagson.TestingUtils.jsonStr;

public class TestExcludedClasses extends TestCase {

    public void testClassLoader() {
        ClassLoader cl = this.getClass().getClassLoader();

        TestingUtils.testWithNullExpected(cl, cl.getClass(), jsonStr("null"));
        TestingUtils.testWithNullExpected(cl, (Class)null, jsonStr("null"));

        assertNull(TestingUtils.testDeserialize(jsonStr("{'@type':'sun.misc.Launcher$AppClassLoader'}"), null));
    }

    public void testClassLoaderClass() {
        Object cl = ClassLoader.class;

        TestingUtils.testFully(cl, jsonStr("'java.lang.ClassLoader'"));
    }

}
