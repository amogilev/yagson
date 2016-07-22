package am.yagson;

import junit.framework.TestCase;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import static am.yagson.TestingUtils.jsonStr;

public class TestWeakReferences extends TestCase {

    // tests that all fields in Reference and SoftReference classes are ignored
    public void testWeakReference() {
        WeakReference<String> obj = new WeakReference<String>("foo");
        TestingUtils.test(obj, jsonStr("{}"));
    }

    public void testSoftReference() {
        SoftReference<String> obj = new SoftReference<String>("foo");
        TestingUtils.test(obj, jsonStr("{}"));
    }

    public void testPhantomReference() {
        PhantomReference<String> obj = new PhantomReference<String>("foo", new ReferenceQueue<String>());
        TestingUtils.test(obj, jsonStr("{}"));
    }

    public void testReferenceQueue() {
        ReferenceQueue obj = new ReferenceQueue<String>();
        TestingUtils.test(obj, jsonStr("{}"));
    }

    // tests that fields in custom references are seruialized

    public void testCustomWeakReference() {
        MyWeakReference<String> obj = new MyWeakReference<String>("foo", "bar");
        TestingUtils.test(obj, jsonStr("{'extra':'bar'}"));
    }

    static class MyWeakReference<T> extends WeakReference<T>  {
        final String extra;

        public MyWeakReference(T referent, String extra) {
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
