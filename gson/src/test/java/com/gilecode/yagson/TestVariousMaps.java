package com.gilecode.yagson;

import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedHashTreeMap;
import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.jar.Attributes;

import static com.gilecode.yagson.TestingUtils.*;

public class TestVariousMaps extends TestCase {

    private static Map<String, String> newTestMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("foo", "bar");
        return map;
    }

    private static SortedMap<String, String> newTestSortedMap() {
        SortedMap<String, String> map = new TreeMap<String, String>();
        map.put("foo", "bar");
        return map;
    }

    private static SortedMap<String, String> newTestSortedMapWithComparator() {
        SortedMap<String, String> map = new TreeMap<String, String>(TestingUtils.MY_STRING_CMP);
        map.put("foo", "bar");
        map.put("xx", "xx");

        assertTrue(map.firstKey().equals("xx"));

        return map;
    }

    private SortedMap<Person, String> newTestComplexSortedMapWithComparator() {
        SortedMap<Person, String> map = new TreeMap<Person, String>(TestingUtils.MY_PERSON_CMP);

        map.put(new Person("Jane", "McDoe"), "bar");
        map.put(new Person("John", "Doe"), "foo");

        assertTrue(map.get(map.firstKey()).equals("foo"));

        return map;
    }


    private static Map<Object, Object> newTestObjMap() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("foo", "bar");
        return map;
    }

    public void testHashMap() {
        Map<String, String> obj = newTestMap();
        TestingUtils.testFully(obj, jsonStr("{'foo':'bar'}"));
    }

    public void testTreeMap() {
        SortedMap<String, String> obj = newTestSortedMap();
        TestingUtils.testFully(obj, jsonStr("{'foo':'bar'}"));
    }

    public void testTreeMapSubMap() {
        SortedMap<String, String> obj = newTestSortedMap().tailMap("foo");
        TestingUtils.testFully(obj, jsonStr(
                "{'m':{'foo':'bar'},'lo':'foo','fromStart':false,'toEnd':true,'loInclusive':true,'hiInclusive':true}"));
    }

    public void testTreeMapWithComparator() {
        SortedMap<String, String> obj = newTestSortedMapWithComparator();
        obj = TestingUtils.testFully(obj, jsonStr("{'xx':'xx','foo':'bar','@.comparator':{'@type':'com.gilecode.yagson.TestingUtils$1','@val':{}}}"));

        assertTrue(obj.firstKey().equals("xx"));
        obj.put("z", "z");
        assertTrue(obj.firstKey().equals("z"));

        obj.clear();
        TestingUtils.testFully(obj, jsonStr("{'@.comparator':{'@type':'com.gilecode.yagson.TestingUtils$1','@val':{}}}"));
    }

    public void testComplexTreeMapWithComparator() {
        SortedMap<Person, String> obj = newTestComplexSortedMapWithComparator();
        obj = TestingUtils.testFully(obj, new TypeToken<TreeMap<Person, String>>(){}, jsonStr(
                "[" +
                        "[{'name':'John','family':'Doe'},'foo']," +
                        "[{'name':'Jane','family':'McDoe'},'bar']," +
                        "{'@.comparator':{'@type':'com.gilecode.yagson.TestingUtils$2','@val':{}}}" +
                        "]"));

        assertTrue(obj.get(obj.firstKey()).equals("foo"));

        obj.clear();
        TestingUtils.testFully(obj, jsonStr("{'@.comparator':{'@type':'com.gilecode.yagson.TestingUtils$2','@val':{}}}"));
    }

    public void testDeserializeComplexTreeMapWithIncorrectExtraFields() {
        try {
            TestingUtils.testDeserialize(
                    jsonStr(
                            "[" +
                                    "[{'name':'John','family':'Doe'},'foo']," +
                                    "[{'name':'Jane','family':'McDoe'},'bar']," +
                                    "{'@.comparator':{'@type':'com.gilecode.yagson.TestingUtils$2','@val':{}}," +
                                    "'@.extraField':'foo'}" +
                                    "]"),
                    new TypeToken<TreeMap<Person, String>>(){}.getType());
            fail("JsonSyntaxException expected");
        } catch (JsonSyntaxException e) {
            assertEquals(
                    "The class java.util.TreeMap does not have serializable reflective field 'extraField'",
                    e.getMessage());
        }

    }


    public void testUnmodifiableMap1() {
        Map<String, String> obj = Collections.unmodifiableMap(newTestMap());
        TestingUtils.testFully(obj, jsonStr(
                "{'m':{'@type':'java.util.HashMap','@val':{'foo':'bar'}}}"));
    }

    public void testUnmodifiableMap2() {
        Map<String, String> obj = Collections.unmodifiableMap(newTestMap());
        TestingUtils.testFully(obj, Map.class, jsonStr(
                "{'@type':'java.util.Collections$UnmodifiableMap','@val':{" +
                        "'m':{'@type':'java.util.HashMap','@val':{'foo':'bar'}}}}"));
    }

    public void testUnmodifiableMapInObject() {
        Map<Object, Object> umap = Collections.unmodifiableMap(newTestObjMap());
        ClassWithMixedMap obj = new ClassWithMixedMap(umap);

        TestingUtils.testFully(obj, jsonStr(
                "{'map':{'@type':'java.util.Collections$UnmodifiableMap','@val':{" +
                        "'m':{'@type':'java.util.HashMap','@val':{'foo':'bar'}}}}}"));
    }

    public void testEmptyMap() {
        Map<String, String> obj = Collections.emptyMap();

        TestingUtils.testFully(obj, jsonStr("{}"));
    }

    public void testSynchronizedMap() {
        Map<String, String> obj = Collections.synchronizedMap(newTestMap());

        TestingUtils.testFully(obj, jsonStr(
                "{'m':{'@type':'java.util.HashMap','@val':{'foo':'bar'}},'mutex':'@root'}"));
    }

    public void testSynchronizedSortedMap() {
        SortedMap<String, String> obj = Collections.synchronizedSortedMap(newTestSortedMap());

        // NOTE: duplication in circular-only refs mode
        TestingUtils.testFully(gsonCircularOnlyMode, obj, jsonStr(
                "{'sm':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}}," +
                        "'m':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}}," +
                        "'mutex':'@root'}"));

        TestingUtils.testFully(gsonCircularAndSiblingMode, obj, jsonStr(
                "{'sm':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}},'m':'@.sm','mutex':'@root'}"));

        TestingUtils.testFully(obj, jsonStr(
                "{'sm':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}},'m':'@.sm','mutex':'@root'}"));
    }

    public void testCheckedMap() {
        Map<String, String> obj = Collections.checkedMap(newTestMap(), String.class, String.class);

        TestingUtils.testFully(obj, jsonStr(
                "{'m':{'@type':'java.util.HashMap','@val':{'foo':'bar'}}," +
                        "'keyType':'java.lang.String','valueType':'java.lang.String'}"));
    }

    public void testCheckedSortedMap() {
        Map<String, String> obj = Collections.checkedSortedMap(newTestSortedMap(), String.class, String.class);

        // NOTE: duplication in circular-only refs mode
        TestingUtils.testFully(gsonCircularOnlyMode, obj, jsonStr(
                "{'sm':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}}," +
                        "'m':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}}," +
                        "'keyType':'java.lang.String','valueType':'java.lang.String'}"));

        TestingUtils.testFully(gsonAllDuplicatesMode, obj, jsonStr(
                "{'sm':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}},'m':'@.sm'," +
                        "'keyType':'java.lang.String','valueType':'java.lang.String'}"));

        TestingUtils.testFully(gsonAllDuplicatesMode, obj, jsonStr(
                "{'sm':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}},'m':'@.sm'," +
                        "'keyType':'java.lang.String','valueType':'java.lang.String'}"));

    }

    public void testSingletonMap() {
        Map<String, String> obj = Collections.singletonMap("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'k':'foo','v':'bar'}"));
    }

    public void testLinkedHashMap() {
        Map<String, String> obj = new LinkedHashMap<String, String>();
        obj.put("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'foo':'bar'}"));
    }

    public void testLinkedHashTreeMap() {
        Map<String, String> obj = new LinkedHashTreeMap<String, String>();
        obj.put("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'foo':'bar'}"));
    }

    public void testLinkedHashTreeMapWithComparator() {
        Map<String, String> obj = new LinkedHashTreeMap<String, String>(TestingUtils.MY_STRING_CMP);
        obj.put("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'foo':'bar','@.comparator':{'@type':'com.gilecode.yagson.TestingUtils$1','@val':{}}}"));
    }

    public void testConcurrentHashMap() {
        Map<String, String> obj = new ConcurrentHashMap<String, String>();
        obj.put("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'foo':'bar'}"));
    }

    public void testConcurrentSkipListMap() {
        Map<String, String> obj = new ConcurrentSkipListMap<String, String>();
        obj.put("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'foo':'bar'}"));
    }

    public void testConcurrentSkipListSubMap() {
        Map<String, String> obj = new ConcurrentSkipListMap<String, String>().descendingMap();
        obj.put("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'m':{'foo':'bar'},'loInclusive':false,'hiInclusive':false,'isDescending':true}"));
    }

    public void testHashtable() {
        Map<String, String> obj = new Hashtable<String, String>();
        obj.put("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'foo':'bar'}"));
    }

    public void testProperties() {
        Map<Object, Object> obj = new Properties();
        obj.put("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'foo':'bar'}"));
    }

    public void testPropertiesWithDefaults() {
        Properties defaults = new Properties();
        defaults.put("foo0", "defaultFoo0");

        Properties obj = new Properties(defaults);
        obj.put("foo", "bar");

        Properties result = TestingUtils.testFully(obj, jsonStr(
                "{'foo':'bar','@.defaults':{'foo0':'defaultFoo0'}}"));
        assertEquals("defaultFoo0", result.getProperty("foo0"));
    }

    public void testAttributes() {
        Attributes obj = new Attributes();
        obj.putValue("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'map':{'@type':'java.util.HashMap','@val':[[" +
                        "{'@type':'java.util.jar.Attributes$Name','@val':{'name':'foo','hashCode':'@hash'}},'bar']]}}"));
    }

    public void testWeakHashMap() {
        WeakHashMap obj = new WeakHashMap();
        obj.put("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'foo':'bar'}"));
    }

    public void testIdentityHashMap() {
        IdentityHashMap obj = new IdentityHashMap();
        obj.put("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'foo':'bar'}"));
    }
}
