package com.gilecode.yagson.tests;

import com.gilecode.yagson.YaGsonBuilder;
import com.gilecode.yagson.tests.data.ClassWithObject;
import com.gilecode.yagson.tests.util.BindingTestCase;
import com.gilecode.yagson.tests.util.EqualityCheckMode;
import com.gilecode.yagson.types.NSLambdaPolicy;
import com.gilecode.yagson.types.NonSerializableLambdaException;
import com.gilecode.yagson.types.TypeUtils;
import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Test for serializable.
 * Only available for Java 8 and higher  - shall be commented out otherwise.
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
