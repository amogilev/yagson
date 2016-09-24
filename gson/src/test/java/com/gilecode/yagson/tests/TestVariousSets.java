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

import com.gilecode.yagson.tests.data.Person;
import com.gilecode.yagson.tests.util.BindingTestCase;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Tests serialization of various standard {@link Set}s from the Java Collections Framework.
 *
 * @author Andrey Mogilev
 */
public class TestVariousSets extends BindingTestCase {

    private static final List<String> foo = Collections.singletonList("foo");
    private static final List<String> foobar = Arrays.asList("foo", "bar");

    private static Set<String> newTestHashSet() {
        return new HashSet<String>(foo);
    }

    private static Set<String> newTestTreeSet() {
        Set<String> obj = new TreeSet<String>();
        obj.add("foo1");
        obj.add("foo22");
        return obj;
    }

    private static SortedSet<String> newTestTreeSetWithComparator() {
        SortedSet<String> obj = new TreeSet<String>(MY_STRING_CMP);
        obj.add("foo1");
        obj.add("foo22");
        return obj;
    }

    //
    // Tests with the special sets from java.util.Collections
    //

    public void testSetFromMap() {
        Set<String> obj = Collections.newSetFromMap(new HashMap<String, Boolean>());
        obj.add("foo");

        test(obj, jsonStr(
                "{'m':{'@type':'java.util.HashMap','@val':{'foo':true}}}"));
        testAsCollection(obj);
    }

    public void testSetFromMapOfPersonWithWrapper() {
        Set<Person> obj = Collections.newSetFromMap(new HashMap<Person, Boolean>());
        obj.add(new Person("John", "Smith"));

        test(obj, new TypeToken<Set<Person>>(){}.getType(), jsonStr(
                "{'@type':'java.util.Collections$SetFromMap','@val':{" +
                        "'m':{'@type':'java.util.HashMap','@val':[[{'name':'John','family':'Smith'},true]]}}}"));
    }

    public void testEmptySet1() {
        Set<String> obj = Collections.emptySet();

        test(obj, jsonStr("[]"));
        testAsCollection(obj);
    }

    public void testEmptySet2() {
        Set<String> obj = Collections.emptySet();

        test(obj, Set.class, jsonStr(
                "{'@type':'java.util.Collections$EmptySet','@val':[]}"));
    }

    public void testSynchronizedSet() {
        Set<String> obj = Collections.synchronizedSet(newTestHashSet());

        test(obj, jsonStr(
                "{'c':{'@type':'java.util.HashSet','@val':['foo']},'mutex':'@root'}"));
        testAsCollection(obj);
    }

    public void testSynchronizedSortedSet() {
        Set<String> obj = Collections.synchronizedSortedSet(newTestTreeSetWithComparator());
        test(obj, jsonStr(
                "{'ss':{'@type':'java.util.TreeSet','@val':['@.m:',{'@.comparator':{'@type':'com.gilecode.yagson.tests.util.BindingTestCase$1','@val':{}}},'foo1','foo22']}," +
                        "'c':'@.ss','mutex':'@root'}"));
        testAsCollection(obj);
    }

    public void testCheckedSet() {
        Set<String> obj = Collections.checkedSet(newTestHashSet(), String.class);

        test(obj, jsonStr(
                "{'c':{'@type':'java.util.HashSet','@val':['foo']},'type':'java.lang.String'}"));
        testAsCollection(obj);
    }

    public void testCheckedSortedSet() {
        Set<String> obj = Collections.checkedSortedSet(newTestTreeSetWithComparator(), String.class);
        test(obj, jsonStr(
                "{'ss':{'@type':'java.util.TreeSet','@val':['@.m:',{'@.comparator':{'@type':'com.gilecode.yagson.tests.util.BindingTestCase$1','@val':{}}},'foo1','foo22']}," +
                        "'c':'@.ss','type':'java.lang.String'}"));
        testAsCollection(obj);
    }

    public void testUnmodifiableSet() {
        Set<String> obj = Collections.unmodifiableSet(newTestHashSet());

        test(obj, jsonStr(
                "{'c':{'@type':'java.util.HashSet','@val':['foo']}}"));
        testAsCollection(obj);
    }

    public void testUnmodifiableSortedSet() {
        Set<String> obj = Collections.unmodifiableSortedSet(newTestTreeSetWithComparator());
        test(obj, jsonStr(
                "{'ss':{'@type':'java.util.TreeSet','@val':['@.m:',{'@.comparator':{'@type':'com.gilecode.yagson.tests.util.BindingTestCase$1','@val':{}}},'foo1','foo22']}," +
                        "'c':'@.ss'}"));
        testAsCollection(obj);
    }

    public void testSingletonSet() {
        Set<String> obj = Collections.singleton("foo");
        test(obj, jsonStr(
                "{'element':'foo'}"));
        testAsCollection(obj);
    }

    //
    // Tests for the general sets
    //

    public void testHashSet() {
        Set<String> obj = newTestHashSet();

        test(obj, jsonStr("['foo']"));
        testAsCollection(HashSet.class);
    }

    public void testLinkedHashSet() {
        Set<String> obj = new LinkedHashSet<String>(foobar);

        test(obj, jsonStr("['foo','bar']"));
        testAsCollection(LinkedHashSet.class);
    }

    public void testTreeSet() {
        Set<String> obj = newTestTreeSet();

        test(obj, jsonStr("['foo1','foo22']"));
        testAsCollection(TreeSet.class);
    }

    public void testTreeSetWithComparator() {
        Set<String> obj = newTestTreeSetWithComparator();

        Set<String> result = test(obj, jsonStr(
                "['@.m:',{'@.comparator':{'@type':'com.gilecode.yagson.tests.util.BindingTestCase$1','@val':{}}},'foo1','foo22']"));

        assertTrue(result.contains("foo1"));
        assertTrue("Requires special PRESENT object as backing map values", result.remove("foo1"));

        obj.clear();
        obj.addAll(collectionsTestsElements);
        testAsCollection(obj);
    }

    public void testTreeSetOfPersonWithComparator() {
        Set<Person> obj = new TreeSet<Person>(MY_PERSON_CMP);
        obj.add(new Person("John", "Smith"));

        test(obj, new TypeToken<TreeSet<Person>>(){}.getType(), jsonStr(
                "['@.m:',{'@.comparator':{'@type':'com.gilecode.yagson.tests.util.BindingTestCase$2','@val':{}}}," +
                        "{'name':'John','family':'Smith'}]"));
        testAsCollection(obj);
    }

    @SuppressWarnings("unchecked")
    public void testTreeSetWithNonDefaultBackingMap() throws Exception {
        NavigableMap<String,Object> m = new ConcurrentSkipListMap<String, Object>();

        Constructor<TreeSet> constr = TreeSet.class.getDeclaredConstructor(NavigableMap.class);
        constr.setAccessible(true);

        TreeSet<String> obj = constr.newInstance(m);
        obj.add("foo");

        test(obj, jsonStr(
                "['@.m:',{'@type':'java.util.concurrent.ConcurrentSkipListMap','@val':{}},'foo']"));
        testAsCollection(obj);
    }

    public void testConcurrentSkipListSet() {
        Set<String> obj = new ConcurrentSkipListSet<String>(foo);

        test(obj, jsonStr("['foo']"));
        testAsCollection(ConcurrentSkipListSet.class);
    }

    public void testCopyOnWriteArraySet() {
        Set<String> obj = new CopyOnWriteArraySet<String>(foo);

        test(obj, jsonStr("['foo']"));
        testAsCollection(CopyOnWriteArraySet.class);
    }
}
