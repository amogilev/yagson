package com.gilecode.yagson.tests;

import com.gilecode.yagson.tests.util.BindingTestCase;
import com.gilecode.yagson.tests.util.EqualityCheckMode;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * Tests serialization of weak/soft references, i.e. that the refernt data is omitted.
 *
 * @author Andrey Mogilev
 */
public class TestWeakReferences extends BindingTestCase {

    // tests that all fields in Reference and SoftReference classes are ignored
    public void testWeakReference() {
        WeakReference<String> obj = new WeakReference<String>("foo");
        obj = test(obj, jsonStr("{}"), EqualityCheckMode.NONE);
        assertNull(obj.get());
    }

    public void testSoftReference() {
        SoftReference<String> obj = new SoftReference<String>("foo");
        obj = test(obj, jsonStr("{}"), EqualityCheckMode.NONE);
        assertNull(obj.get());
    }

    public void testPhantomReference() {
        PhantomReference<String> obj = new PhantomReference<String>("foo", new ReferenceQueue<String>());
        obj = test(obj, jsonStr("{}"), EqualityCheckMode.NONE);
        assertNull(obj.get());
    }

    public void testReferenceQueue() {
        ReferenceQueue obj = new ReferenceQueue<String>();
        obj = test(obj, jsonStr("{}"), EqualityCheckMode.NONE);
        assertNull(obj.poll());
    }

    // tests that fields in custom references are seruialized

    public void testCustomWeakReference() {
        MyWeakReference<String> obj = new MyWeakReference<String>("foo", "bar");
        obj = test(obj, jsonStr("{'extra':'bar'}"), EqualityCheckMode.NONE);
        assertNull(obj.get());
        assertEquals("bar", obj.extra);
    }

    private static class MyWeakReference<T> extends WeakReference<T>  {
        final String extra;

        MyWeakReference(T referent, String extra) {
            super(referent);
            this.extra = extra;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyWeakReference<?> that = (MyWeakReference<?>) o;
            return extra.equals(that.extra);

        }

        @Override
        public int hashCode() {
            return extra.hashCode();
        }
    }
}
