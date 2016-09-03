package am.yagson;

import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;
import sun.awt.util.IdentityArrayList;
import sun.awt.util.IdentityLinkedList;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static am.yagson.TestingUtils.jsonStr;
import static java.util.Arrays.asList;

public class TestVariousLists extends TestCase {

    //
    // Tests special lists from java.util.Collections
    //

    public void testUnmodifiableList() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        List<Long> obj = Collections.unmodifiableList(l);
        TestingUtils.testFully(obj, jsonStr("{'list':[1],'c':'@.list'}"));
    }

    public void testCheckedList() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        List<Long> obj = Collections.checkedList(l, Long.class);
        TestingUtils.testFully(obj, jsonStr("{'list':[1],'c':'@.list','type':'java.lang.Long'}"));
    }

    public void testSynchronizedList() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        List<Long> obj = Collections.synchronizedList(l);
        TestingUtils.testFully(obj, jsonStr("{'list':[1],'c':'@.list','mutex':'@root'}"));
    }

    public void testEmptyList() {
        List<Long> obj = Collections.emptyList();
        TestingUtils.testFully(obj, jsonStr("[]"));
    }

    public void testSingletonList() {
        List<Long> obj = Collections.singletonList(1L);
        TestingUtils.testFully(obj, jsonStr("{'element':1}"));
    }

    public void testListFromEnumeration() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        Enumeration<Long> e = Collections.enumeration(l);
        List<Long> obj = Collections.list(e);
        TestingUtils.testFully(obj, jsonStr("[1]"));
    }

    public void testNCopiesList() {
        List<Long> obj = Collections.nCopies(3, 1L);
        TestingUtils.testFully(obj, jsonStr("{'n':3,'element':1}"));
    }

    //
    // Tests common lists
    //

    public void testArraysAsList() {
        List<Long> l = asList(1L, 2L, 3L);

        TestingUtils.testFully(l, jsonStr(
                "{'a':{'@type':'[Ljava.lang.Long;','@val':[1,2,3]}}"));

        TestingUtils.testFully(l, new TypeToken<List<Long>>(){}, jsonStr(
                "{'@type':'java.util.Arrays$ArrayList','@val':{'a':[1,2,3]}}"));

    }

    public void testArrayList() {
        List<Long> l = new ArrayList<Long>(asList(1L, 2L, 3L));

        TestingUtils.testFully(l, jsonStr("[1,2,3]"));
    }

    public void testIdentityArrayList() {
        List<Long> l = new IdentityArrayList<Long>(asList(1L, 1L, 2L));

        TestingUtils.testFully(l, jsonStr("[1,1,2]"));
    }

    public void testLinkedList() {
        List<Long> l = new LinkedList<Long>(asList(1L, 2L, 3L));

        TestingUtils.testFully(l, jsonStr("[1,2,3]"));
    }

    public void testIdentityLinkedList() {
        List<Long> l = new IdentityLinkedList<Long>(asList(1L, 1L, 2L));

        TestingUtils.testFully(l, jsonStr("[1,1,2]"));
    }

    public void testVector() {
        List<Long> l = new Vector<Long>(asList(1L, 2L, 3L));

        TestingUtils.testFully(l, jsonStr("[1,2,3]"));
    }

    public void testStack() {
        Stack<Long> l = new Stack<Long>();
        l.addAll(asList(1L, 2L, 3L));

        TestingUtils.testFully(l, jsonStr("[1,2,3]"));
    }

    public void testCopyOnWriteArrayList() {
        List<Long> l = new CopyOnWriteArrayList<Long>(asList(1L, 2L));
        l.add(3L);

        TestingUtils.testFully(l, jsonStr("[1,2,3]"));
    }

    //
    // Tests sub-lists
    //

    public void testSubList() {
        List<Long> l = new LinkedList<Long>();
        l.add(1L);
        l.add(2L);
        l.add(3L);
        List<Long> obj = l.subList(1, 2);

        TestingUtils.testFully(obj, Object.class, jsonStr(
                "{'@type':'java.util.SubList','@val':{'l':{'@type':'java.util.LinkedList','@val':[1,2,3]}," +
                        "'offset':1,'size':1}}"));
    }

    public void testRandomAccessSubList() {
        List<Long> l = new ArrayList<Long>(asList(1L, 2L, 3L));
        List<Long> obj = l.subList(1, 2);

        TestingUtils.testFully(obj, Object.class, jsonStr(
                "{'@type':'java.util.RandomAccessSubList','@val':{'l':{'@type':'java.util.ArrayList','@val':[1,2,3]}," +
                        "'offset':1,'size':1}}"));
    }

    public void testCOWSubList() {
        List<Long> l = new CopyOnWriteArrayList<Long>(asList(1L, 2L, 3L));
        List<Long> obj = l.subList(1, 2);

        TestingUtils.testFully(obj, Object.class, jsonStr(
                "{'@type':'java.util.concurrent.CopyOnWriteArrayList$COWSubList','@val':{'l':[1,2,3]," +
                        "'offset':1,'size':1}}"));
    }
}
