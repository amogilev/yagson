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

import com.gilecode.yagson.YaGsonBuilder;
import com.gilecode.yagson.tests.data.ClassWithObject;
import com.gilecode.yagson.tests.util.BindingTestCase;
import com.gilecode.yagson.tests.util.EqualityCheckMode;
import com.gilecode.yagson.types.NSLambdaPolicy;
import com.gilecode.yagson.types.NonSerializableLambdaException;
import com.gilecode.yagson.types.TypeUtils;
import com.google.gson.Gson;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Test for non-serializable lambdas.
 *
 * @author Andrey Mogilev
 */
public class TestNonSerializableLambdas extends BindingTestCase {

    public void testLambdaDetection() {
        Supplier s1 = (Supplier) () -> "foo";
        assertTrue(TypeUtils.isLambdaClass(s1.getClass()));

        Predicate<Object> mref1 = this::equals;
        assertTrue(TypeUtils.isLambdaClass(mref1.getClass()));

        Consumer<String> mref2 = System.out::println;
        assertTrue(TypeUtils.isLambdaClass(mref2.getClass()));

        LongSupplier mref3 = System::currentTimeMillis;
        assertTrue(TypeUtils.isLambdaClass(mref3.getClass()));

        assertFalse(TypeUtils.isLambdaClass(Object.class));
        assertFalse(TypeUtils.isLambdaClass(this.getClass()));
    }

    public void testNonSerializableLambdaSkipMode() {
        Supplier s1 = (Supplier) () -> "foo";
        test(s1, "null", EqualityCheckMode.EXPECT_NULL);

        Supplier[] arr = {s1};
        test(arr, jsonStr("[null]"), EqualityCheckMode.NONE);

        ClassWithObject obj = new ClassWithObject(s1);
        test(obj, jsonStr("{}"), EqualityCheckMode.NONE);
    }

    public void testNonSerializableLambdaErrorMode() {
        Gson gson = new YaGsonBuilder().setNsLambdaPolicy(NSLambdaPolicy.ERROR).create();
        Supplier s1 = (Supplier) () -> "foo";
        try {
            test(gson, s1);
            fail("NonSerializableLambdaException expected!");
        } catch (NonSerializableLambdaException e) {
            // expected;
        }

        Supplier[] arr = {s1};
        try {
            test(gson, arr);
            fail("NonSerializableLambdaException expected!");
        } catch (NonSerializableLambdaException e) {
            // expected;
        }

        ClassWithObject obj = new ClassWithObject(s1);
        try {
            test(gson, obj);
            fail("NonSerializableLambdaException expected!");
        } catch (NonSerializableLambdaException e) {
            // expected;
        }
    }

    public void testNSLambdaInSetComparator() {
        Comparator<String> cmp1 = (s1, s2) -> s1.length() - s2.length();
        Comparator<String> cmp2 = Comparator.nullsFirst(cmp1);

        Set<String> set = new TreeSet<>(cmp2);
        test(set, "[]", EqualityCheckMode.NONE);

        Map<String, String> map = new TreeMap<>(cmp2);
        test(map, "{}", EqualityCheckMode.NONE);
    }
}
