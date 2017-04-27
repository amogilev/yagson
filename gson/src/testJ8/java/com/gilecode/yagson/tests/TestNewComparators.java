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
import java.util.*;
import java.util.function.Function;

import static java.util.Arrays.asList;

/**
 * Tests sets/maps with Java8 new-style comparators.
 *
 * @author Andrey Mogilev
 */
public class TestNewComparators extends BindingTestCase {

    public void testComparator1() {
        Comparator<String> cmp = Comparator.nullsFirst(Comparator.reverseOrder());
        SortedSet<String> obj = new TreeSet<>(cmp);
        obj.add("foo1");
        obj.add("foo22");

        Set<String> result = test(obj, jsonStr("['@.m:',{'@.comparator':" +
                "{'@type':'java.util.Comparators$NullComparator'," +
                "'@val':{'nullFirst':true,'real':{'@type':'java.util.Collections$ReverseComparator','@val':{}}}}}," +
                "'foo22','foo1']"));

        assertTrue(result.contains("foo1"));
        assertTrue("Requires special PRESENT object as backing map values", result.remove("foo1"));

        obj.clear();
        obj.addAll(collectionsTestsElements);
        testAsCollection(obj);
    }

    public void testLambdaComparator() {
        Comparator<String> cmp = Comparator.comparing((Function<String, Integer> & Serializable)String::length);
        SortedSet<String> obj = new TreeSet<>(cmp);
        obj.add("foo111");
        obj.add("foo2");
        assertEquals(asList("foo2", "foo111"), new ArrayList<>(obj));

        Set<String> result = test(obj, null, EqualityCheckMode.NONE);

        assertEquals(asList("foo2", "foo111"), new ArrayList<>(result));

        assertTrue(result.contains("foo111"));
        assertTrue("Requires special PRESENT object as backing map values", result.remove("foo111"));

        obj.clear();
        obj.addAll(collectionsTestsElements);
        testAsCollection(obj);
    }

}
