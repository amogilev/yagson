package com.gilecode.yagson.tests;

import com.gilecode.yagson.tests.util.BindingTestCase;
import com.google.gson.reflect.TypeToken;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Arrays.asList;

/**
 * Tests serialization of various standard {@link List}s from the Java Collections Framework.
 *
 * @author Andrey Mogilev
 */
public class TestVariousLists extends BindingTestCase {

    //
    // Tests special lists from java.util.Collections
    //

    public void testUnmodifiableList() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        List<Long> obj = Collections.unmodifiableList(l);
        test(obj, jsonStr("{'list':[1],'c':'@.list'}"));
        testAsCollection(obj);
    }

    public void testCheckedList() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        List<Long> obj = Collections.checkedList(l, Long.class);
        test(obj, jsonStr("{'list':[1],'c':'@.list','type':'java.lang.Long'}"));
        testAsCollection(obj);
    }

    public void testSynchronizedList() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        List<Long> obj = Collections.synchronizedList(l);
        test(obj, jsonStr("{'list':[1],'c':'@.list','mutex':'@root'}"));
        testAsCollection(obj);
    }

    public void testEmptyList() {
        List<Long> obj = Collections.emptyList();
        test(obj, jsonStr("[]"));
        testAsCollection(obj);
    }

    public void testSingletonList() {
        List<Long> obj = Collections.singletonList(1L);
        test(obj, jsonStr("{'element':1}"));
        testAsCollection(obj);
    }

    public void testListFromEnumeration() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        Enumeration<Long> e = Collections.enumeration(l);
        List<Long> obj = Collections.list(e);
        test(obj, jsonStr("[1]"));
        testAsCollection(obj);
    }

    public void testNCopiesList() {
        List<Long> obj = Collections.nCopies(3, 1L);
        test(obj, jsonStr("{'n':3,'element':1}"));
        testAsCollection(obj);
    }

    //
    // Tests common lists
    //

    public void testArraysAsList() {
        List<Long> l = asList(1L, 2L, 3L);

        test(l, jsonStr(
                "{'a':{'@type':'[Ljava.lang.Long;','@val':[1,2,3]}}"));

        test(l, new TypeToken<List<Long>>(){}.getType(), jsonStr(
                "{'@type':'java.util.Arrays$ArrayList','@val':{'a':[1,2,3]}}"));
        testAsCollection(l);
    }

    public void testArrayList() {
        List<Long> l = new ArrayList<Long>(asList(1L, 2L, 3L));

        test(l, jsonStr("[1,2,3]"));
        testAsCollection(ArrayList.class);
    }

    public void testLinkedList() {
        List<Long> l = new LinkedList<Long>(asList(1L, 2L, 3L));

        test(l, jsonStr("[1,2,3]"));
        testAsCollection(LinkedList.class);
    }

    public void testVector() {
        List<Long> l = new Vector<Long>(asList(1L, 2L, 3L));

        test(l, jsonStr("[1,2,3]"));
        testAsCollection(Vector.class);
    }

    public void testStack() {
        Stack<Long> l = new Stack<Long>();
        l.addAll(asList(1L, 2L, 3L));

        test(l, jsonStr("[1,2,3]"));
        testAsCollection(Stack.class);
    }

    public void testCopyOnWriteArrayList() {
        List<Long> l = new CopyOnWriteArrayList<Long>(asList(1L, 2L));
        l.add(3L);

        test(l, jsonStr("[1,2,3]"));
        testAsCollection(CopyOnWriteArrayList.class);
    }
}
