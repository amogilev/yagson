package am.yagson;

import junit.framework.TestCase;

import static am.yagson.TestingUtils.jsonStr;

public class TestExcludedClasses extends TestCase {

    public void testRootClassLoader() {
        ClassLoader cl = this.getClass().getClassLoader();

        TestingUtils.testWithNullExpected(cl, cl.getClass(), jsonStr("null"));
        TestingUtils.testWithNullExpected(cl, (Class)null, jsonStr("null"));

        assertNull(TestingUtils.testDeserialize(jsonStr("{'@type':'sun.misc.Launcher$AppClassLoader'}"), null));
    }

    public void testFieldClassLoader() {
        ClassLoader cl = this.getClass().getClassLoader();
        ClassWithObject obj = new ClassWithObject(cl);

        obj = TestingUtils.test(obj, jsonStr("{}"));
        assertNull(obj.obj);
    }

    public void testClassLoaderClass() {
        Object cl = ClassLoader.class;

        TestingUtils.testFully(cl, jsonStr("'java.lang.ClassLoader'"));
    }

}
