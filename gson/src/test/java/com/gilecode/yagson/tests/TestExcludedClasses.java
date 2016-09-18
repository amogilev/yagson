package com.gilecode.yagson.tests;

import com.gilecode.yagson.YaGson;
import com.gilecode.yagson.tests.data.ClassWithObject;
import com.gilecode.yagson.tests.util.BindingTestCase;
import com.gilecode.yagson.tests.util.EqualityCheckMode;

/**
 * Tests for serialization of excluded classes, i.e. special classes which are serialized to {@code null}s.
 *
 * @author Andrey Mogilev
 */
public class TestExcludedClasses extends BindingTestCase {

    public void testRootClassLoader() {
        ClassLoader cl = this.getClass().getClassLoader();

        test(cl, cl.getClass(), "null", EqualityCheckMode.EXPECT_NULL);
        test(cl, (Class)null, "null", EqualityCheckMode.EXPECT_NULL);

        assertNull(new YaGson().fromJson(jsonStr("{'@type':'sun.misc.Launcher$AppClassLoader'}"), Object.class));
    }

    public void testFieldClassLoader() {
        ClassLoader cl = this.getClass().getClassLoader();
        ClassWithObject obj = new ClassWithObject(cl);

        obj = test(obj, jsonStr("{}"), EqualityCheckMode.NONE);
        assertNull(obj.obj);
    }

    public void testClassLoaderClass() {
        Object cl = ClassLoader.class;

        test(cl, jsonStr("'java.lang.ClassLoader'"));
    }
}
