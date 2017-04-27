/*
 * Copyright (C) 2017 Andrey Mogilev
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Test for serializable.
 *
 * @author Andrey Mogilev
 */
public class TestSerializableLambdas extends BindingTestCase {

    public void testSerializableLambda1() throws Exception {
        Supplier s1 = (Supplier & Serializable) () -> "foo";

        // NOTE: actual JSON is fragile and may change on test changes
        Supplier s2 = test(s1, jsonStr("{'@type':'java.lang.invoke.SerializedLambda','@val':{" +
                "'capturingClass':'com.gilecode.yagson.tests.TestSerializableLambdas'," +
                "'functionalInterfaceClass':'java/util/function/Supplier','functionalInterfaceMethodName':'get'," +
                "'functionalInterfaceMethodSignature':'()Ljava/lang/Object;'," +
                "'implClass':'com/gilecode/yagson/tests/TestSerializableLambdas'," +
                "'implMethodName':'lambda$testSerializableLambda1$c9303e5c$1'," +
                "'implMethodSignature':'()Ljava/lang/Object;'," +
                "'implMethodKind':6,'instantiatedMethodType':'()Ljava/lang/Object;'," +
                "'capturedArgs':[]}}"),
                EqualityCheckMode.NONE);
        assertEquals("foo", s2.get());
    }

    public void testSerializableLambda2() throws Exception {
        AtomicInteger cnt = new AtomicInteger(1);
        Supplier s1 = (Supplier & Serializable) () -> "foo" + cnt;
        Supplier s2 = test(s1, null, EqualityCheckMode.NONE);

        assertEquals("foo1", s2.get());

        // however, dynamic changes of counter are not supported:
        cnt.set(2);
        assertEquals("foo2", s1.get());
        assertEquals("foo1", s2.get());
    }

    public void testSerializableLambda3() throws Exception {
        Supplier<ArrayList> s1 = (Supplier & Serializable) ArrayList::new;
        Supplier<ArrayList> s2 = test(s1, null, EqualityCheckMode.NONE);

        ArrayList list1 = s2.get();
        ArrayList list2 = s2.get();

        assertFalse(list1 == list2);
        assertNotNull(list1);
        assertNotNull(list2);
    }
}
