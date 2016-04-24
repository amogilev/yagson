package am.yagson;

import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import static am.yagson.TestingUtils.jsonStr;

public class TestVariousSets extends TestCase {

    private static Set<String> newTestHashSet() {
        Set<String> obj = new HashSet<String>();
        obj.add("foo");
        return obj;
    }

    private static Set<String> newTestTreeSet() {
        Set<String> obj = new TreeSet<String>();
        obj.add("foo1");
        obj.add("foo22");
        return obj;
    }

    private static SortedSet<String> newTestTreeSetWithComparator() {
        SortedSet<String> obj = new TreeSet<String>(TestingUtils.MY_STRING_CMP);
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

        TestingUtils.testFully(obj, jsonStr("{'@vtype':'java.util.HashMap','m':{'foo':true}}"));
    }

    public void testSetFromMapOfPersonWithWrapper() {
        Set<Person> obj = Collections.newSetFromMap(new HashMap<Person, Boolean>());
        obj.add(new Person("John", "Smith"));

        TestingUtils.testFully(obj, new TypeToken<Set<Person>>(){}, jsonStr(
                "{'@type':'java.util.Collections$SetFromMap','@val':{'@vtype':'java.util.HashMap'," +
                        "'m':[[{'name':'John','family':'Smith'},true]]}}"));
    }

    public void testEmptySet1() {
        Set<String> obj = Collections.emptySet();

        TestingUtils.testFully(obj, jsonStr("[]"));
    }

    public void testEmptySet2() {
        Set<String> obj = Collections.emptySet();

        Set<String> result = TestingUtils.testFully(obj, Set.class, jsonStr(
                "{'@type':'java.util.Collections$EmptySet','@val':[]}"));
        System.out.println("result = " + result);
    }

    public void testSynchronizedSet() {
        Set<String> obj = Collections.synchronizedSet(newTestHashSet());

        TestingUtils.testFully(obj, jsonStr(
                "{'@vtype':'java.util.HashSet','c':['foo'],'mutex':'@root'}"));
    }

    public void testSynchronizedSortedSet() {
        Set<String> obj = Collections.synchronizedSortedSet(newTestTreeSetWithComparator());
        TestingUtils.testFully(obj, jsonStr(
                "{" +
                        "'@vtype':'java.util.TreeSet','ss':['@.m:',{'@.comparator':{'@type':'am.yagson.TestingUtils$1','@val':{}}},'foo1','foo22']," +
                        "'c':'@.ss','mutex':'@root'}"));
    }

    public void testCheckedSet() {
        Set<String> obj = Collections.checkedSet(newTestHashSet(), String.class);

        TestingUtils.testFully(obj, jsonStr(
                "{'@vtype':'java.util.HashSet','c':['foo'],'type':'java.lang.String'}"));
    }

    public void testCheckedSortedSet() {
        Set<String> obj = Collections.checkedSortedSet(newTestTreeSetWithComparator(), String.class);
        TestingUtils.testFully(obj, jsonStr(
                "{" +
                        "'@vtype':'java.util.TreeSet','ss':['@.m:',{'@.comparator':{'@type':'am.yagson.TestingUtils$1','@val':{}}},'foo1','foo22']," +
                        "'c':'@.ss','type':'java.lang.String'}"));
    }

    public void testCheckedMapEntrySet() {
        Map<String, String> map = Collections.checkedMap(new TreeMap<String, String>(), String.class, String.class);
        map.put("foo1", "foo2");

        Set<Map.Entry<String, String>> obj = map.entrySet();

        TestingUtils.testFully(obj, jsonStr(
                "{'@vtype':'java.util.TreeMap$EntrySet','s':{'this$0':{'foo1':'foo2'}},'valueType':'java.lang.String'}"));
    }

    public void testUnmodifiableSet() {
        Set<String> obj = Collections.unmodifiableSet(newTestHashSet());

        TestingUtils.testFully(obj, jsonStr(
                "{'@vtype':'java.util.HashSet','c':['foo']}"));
    }

    public void testUnmodifiableSortedSet() {
        Set<String> obj = Collections.unmodifiableSortedSet(newTestTreeSetWithComparator());
        TestingUtils.testFully(obj, jsonStr(
                "{" +
                        "'@vtype':'java.util.TreeSet','ss':['@.m:',{'@.comparator':{'@type':'am.yagson.TestingUtils$1','@val':{}}},'foo1','foo22']," +
                        "'c':'@.ss'}"));
    }

    public void testUnmodifiableMapEntrySet() {
        Map<String, String> map = new TreeMap<String, String>();
        map.put("foo1", "foo2");
        map = Collections.unmodifiableMap(map);

        Set<Map.Entry<String, String>> obj = map.entrySet();

        TestingUtils.testFully(obj, jsonStr(
                "{'@vtype':'java.util.TreeMap$EntrySet','c':{'this$0':{'foo1':'foo2'}}}"));
    }

    public void testSingletonSet() {
        Set<String> obj = Collections.singleton("foo");
        TestingUtils.testFully(obj, jsonStr(
                "{'element':'foo'}"));
    }

    public void testSingletonMapEntrySet() {
        Map<String, String> map = Collections.singletonMap("foo1", "foo2");
        Set<Map.Entry<String, String>> obj = map.entrySet();

        TestingUtils.testFully(obj, jsonStr(
                "{'@vtype':'java.util.AbstractMap$SimpleImmutableEntry','element':{'key':'foo1','value':'foo2'}}"));
    }

    //
    // Tests for the general sets
    //

    public void testHashSet() {
        Set<String> obj = newTestHashSet();

        TestingUtils.testFully(obj, jsonStr("['foo']"));
    }

    public void testLinkedHashSet() {
        Set<String> obj = new LinkedHashSet<String>();
        obj.add("foo1");
        obj.add("foo2");

        TestingUtils.testFully(obj, jsonStr("['foo1','foo2']"));
    }

    public void testTreeSet() {
        Set<String> obj = newTestTreeSet();

        TestingUtils.testFully(obj, jsonStr("['foo1','foo22']"));
    }

    public void testTreeSetWithComparator() {
        Set<String> obj = newTestTreeSetWithComparator();

        Set<String> result = TestingUtils.testFully(obj, jsonStr(
                "['@.m:',{'@.comparator':{'@type':'am.yagson.TestingUtils$1','@val':{}}},'foo1','foo22']"));

        assertTrue(result.contains("foo1"));
        assertTrue("Requires special PRESENT object as backing map values", result.remove("foo1"));
    }

    public void testTreeSetOfPersonWithComparator() {
        Set<Person> obj = new TreeSet<Person>(TestingUtils.MY_PERSON_CMP);
        obj.add(new Person("John", "Smith"));

        TestingUtils.testFully(obj, new TypeToken<TreeSet<Person>>(){}, jsonStr(
                "['@.m:',{'@.comparator':{'@type':'am.yagson.TestingUtils$2','@val':{}}}," +
                        "{'name':'John','family':'Smith'}]"));
    }

    public void testTreeSetWithNonDefaultBackingMap() throws Exception {
        NavigableMap<String,Object> m = new ConcurrentSkipListMap<String, Object>();

        Constructor<TreeSet> constr = TreeSet.class.getDeclaredConstructor(NavigableMap.class);
        constr.setAccessible(true);

        TreeSet obj = constr.newInstance(m);
        obj.add("foo");

        TestingUtils.testFully(obj, jsonStr(
                "['@.m:',{'@type':'java.util.concurrent.ConcurrentSkipListMap','@val':{}},'foo']"));
    }

    public void testConcurrentSkipListSet() {
        Set<String> obj = new ConcurrentSkipListSet<String>();
        obj.add("foo");

        TestingUtils.testFully(obj, jsonStr("['foo']"));
    }

    public void testCopyOnWriteArraySet() {
        Set<String> obj = new CopyOnWriteArraySet<String>();
        obj.add("foo");

        TestingUtils.testFully(obj, jsonStr("['foo']"));
    }

    //
    // Tests for the entry and key sets of the general sets
    //

    public void testHashMapKeySet() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("foo1", "foo2");

        Set<String> obj = map.keySet();

        TestingUtils.testFully(obj, jsonStr(
                "{'this$0':{'foo1':'foo2'}}"));
    }

    public void testHashMapEntrySet() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("foo1", "foo2");

        Set<Map.Entry<String, String>> obj = map.entrySet();

        TestingUtils.testFully(obj, jsonStr(
                "{'this$0':{'foo1':'foo2'}}"));
    }

    public void testTreeMapKeySet() {
        Map<String, String> map = new TreeMap<String, String>();
        map.put("foo1", "foo2");

        Set<String> obj = map.keySet();

        TestingUtils.testFully(obj, jsonStr(
                "{'@vtype':'java.util.TreeMap','m':{'foo1':'foo2'}}"));
    }

    public void testTreeMapEntrySet() {
        Map<String, String> map = new TreeMap<String, String>();
        map.put("foo1", "foo2");

        Set<Map.Entry<String, String>> obj = map.entrySet();

        TestingUtils.testFully(obj, jsonStr(
                "{'this$0':{'foo1':'foo2'}}"));
    }

    public void testEmptyMapSets() {
        Map<String, String> map = Collections.emptyMap();

        TestingUtils.testFully(map.keySet(), jsonStr(
                "[]"));
        TestingUtils.testFully(map.entrySet(), jsonStr(
                "[]"));
    }

    public void testSingletonMapSets() {
        Map<String, String> map = Collections.singletonMap("foo", "bar");

        TestingUtils.testFully(map.keySet(), jsonStr(
                "{'element':'foo'}"));
        TestingUtils.testFully(map.entrySet(), jsonStr(
                "{'@vtype':'java.util.AbstractMap$SimpleImmutableEntry','element':{'key':'foo','value':'bar'}}"));
    }

    public void testEnumMapSets() {
        Map<TimeUnit, String> map = new EnumMap<TimeUnit, String>(TimeUnit.class);
        map.put(TimeUnit.DAYS, "days");

        TestingUtils.testFully(map.keySet(), Object.class, jsonStr(
                "{'@type':'java.util.EnumMap$KeySet','@val':{'@vtype':'java.util.EnumMap<java.util.concurrent.TimeUnit,?>','this$0':{'DAYS':'days'}}}"));
        TestingUtils.testFully(map.keySet(), jsonStr(
                "{'@vtype':'java.util.EnumMap<java.util.concurrent.TimeUnit,?>','this$0':{'DAYS':'days'}}"));
        TestingUtils.testFully(map.entrySet(), jsonStr(
                "{'@vtype':'java.util.EnumMap<java.util.concurrent.TimeUnit,?>','this$0':{'DAYS':'days'}}"));
    }
}
