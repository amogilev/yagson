/*
 * Copyright (C) 2016 Andrey Mogilev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
