package am.yagson;

import am.yagson.refs.ReferencesPolicy;
import am.yagson.types.TypeInfoPolicy;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedHashTreeMap;
import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.jar.Attributes;

import static am.yagson.TestingUtils.jsonStr;

public class TestCustomMaps extends TestCase {

    // instances of YaGson for two testing with alternative references policy
    private static YaGson gsonAllDuplicatesMode = new YaGsonBuilder()
            .setReferencesPolicy(ReferencesPolicy.DUPLICATE_OBJECTS)
            .create();

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

    private static final Comparator<String> MY_STRING_CMP = new Comparator<String>() {
        public int compare(String s1, String s2) {
            int cmp = s1.length() - s2.length();
            if (cmp == 0) {
                cmp = s1.compareTo(s2);
            }
            return cmp;
        }
    };

    private static final Comparator<Person> MY_PERSON_CMP = new Comparator<Person>() {
        public int compare(Person o1, Person o2) {
            int cmp = MY_STRING_CMP.compare(o1.family, o2.family);
            if (cmp == 0) {
                cmp = MY_STRING_CMP.compare(o1.name, o2.name);
            }
            return cmp;
        }
    };

    private static SortedMap<String, String> newTestSortedMapWithComparator() {
        SortedMap<String, String> map = new TreeMap<String, String>(MY_STRING_CMP);
        map.put("foo", "bar");
        map.put("xx", "xx");

        assertTrue(map.firstKey().equals("xx"));

        return map;
    }

    private SortedMap<Person, String> newTestComplexSortedMapWithComparator() {
        SortedMap<Person, String> map = new TreeMap<Person, String>(MY_PERSON_CMP);

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
        obj = TestingUtils.testFully(obj, jsonStr("{'xx':'xx','foo':'bar','@.comparator':{'@type':'am.yagson.TestCustomMaps$1','@val':{}}}"));

        assertTrue(obj.firstKey().equals("xx"));
        obj.put("z", "z");
        assertTrue(obj.firstKey().equals("z"));

        obj.clear();
        TestingUtils.testFully(obj, jsonStr("{'@.comparator':{'@type':'am.yagson.TestCustomMaps$1','@val':{}}}"));
    }

    public void testComplexTreeMapWithComparator() {
        SortedMap<Person, String> obj = newTestComplexSortedMapWithComparator();
        obj = TestingUtils.testFully(obj, new TypeToken<TreeMap<Person, String>>(){}, jsonStr(
                "[" +
                        "[{'name':'John','family':'Doe'},'foo']," +
                        "[{'name':'Jane','family':'McDoe'},'bar']," +
                        "{'@.comparator':{'@type':'am.yagson.TestCustomMaps$2','@val':{}}}" +
                        "]"));

        assertTrue(obj.get(obj.firstKey()).equals("foo"));

        obj.clear();
        TestingUtils.testFully(obj, jsonStr("{'@.comparator':{'@type':'am.yagson.TestCustomMaps$2','@val':{}}}"));
    }

    public void testDeserializeComplexTreeMapWithIncorrectExtraFields() {
        try {
            TestingUtils.testDeserialize(
                    new YaGson(),
                    jsonStr(
                            "[" +
                                    "[{'name':'John','family':'Doe'},'foo']," +
                                    "[{'name':'Jane','family':'McDoe'},'bar']," +
                                    "{'@.comparator':{'@type':'am.yagson.TestCustomMaps$2','@val':{}}," +
                                    "'@.extraField':'foo'}" +
                                    "]"),
                    null,
                    new TypeToken<TreeMap<Person, String>>(){});
            fail("JsonSyntaxException expected");
        } catch (JsonSyntaxException e) {
            assertEquals(
                    "The Map type for class java.util.TreeMap does not have serializable reflective field 'extraField'",
                    e.getMessage());
        }

    }


    public void testUnmodifiableMap1() {
        Map<String, String> obj = Collections.unmodifiableMap(newTestMap());
        TestingUtils.testFully(obj, jsonStr("{'@vtype':'java.util.HashMap','m':{'foo':'bar'}}"));
    }

    public void testUnmodifiableMap2() {
        Map<String, String> obj = Collections.unmodifiableMap(newTestMap());
        TestingUtils.testFully(obj, Map.class, jsonStr(
                "{'@type':'java.util.Collections$UnmodifiableMap','@val':{'@vtype':'java.util.HashMap','m':{'foo':'bar'}}}"));
    }

    public void testUnmodifiableMapInObject() {
        Map<Object, Object> umap = Collections.unmodifiableMap(newTestObjMap());
        ClassWithMixedMap obj = new ClassWithMixedMap(umap);

        TestingUtils.testFully(obj, jsonStr(
                "{'@vtype':'java.util.Collections$UnmodifiableMap','map':{'@vtype':'java.util.HashMap','m':{'foo':'bar'}}}"));
    }

    public void testEmptyMap() {
        Map<String, String> obj = Collections.emptyMap();

        TestingUtils.testFully(obj, jsonStr("{}"));
    }

    public void testSynchronizedMap() {
        Map<String, String> obj = Collections.synchronizedMap(newTestMap());

        TestingUtils.testFully(obj, jsonStr(
                "{'@vtype':'java.util.HashMap','m':{'foo':'bar'}," +
                        "'@vtype':'java.util.Collections$SynchronizedMap','mutex':'@root'}"));
    }

    public void testSynchronizedSortedMap() {
        SortedMap<String, String> obj = Collections.synchronizedSortedMap(newTestSortedMap());

        // NOTE: duplication in circular-only refs mode
        TestingUtils.testFully(obj, jsonStr(
                "{'@vtype':'java.util.TreeMap','sm':{'foo':'bar'},'@vtype':'java.util.TreeMap','m':{'foo':'bar'}," +
                        "'@vtype':'java.util.Collections$SynchronizedSortedMap','mutex':'@root'}"));

        TestingUtils.testFully(gsonAllDuplicatesMode, obj, jsonStr(
                "{'@vtype':'java.util.TreeMap','sm':{'foo':'bar'},'@vtype':'java.util.TreeMap','m':'@root.sm'," +
                        "'@vtype':'java.util.Collections$SynchronizedSortedMap','mutex':'@root'}"));
    }

    public void testCheckedMap() {
        Map<String, String> obj = Collections.checkedMap(newTestMap(), String.class, String.class);

        TestingUtils.testFully(obj, jsonStr(
                "{'@vtype':'java.util.HashMap','m':{'foo':'bar'}," +
                        "'keyType':'java.lang.String','valueType':'java.lang.String'}"));
    }

    public void testCheckedSortedMap() {
        Map<String, String> obj = Collections.checkedSortedMap(newTestSortedMap(), String.class, String.class);

        // NOTE: duplication in circular-only refs mode
        TestingUtils.testFully(obj, jsonStr(
                "{'@vtype':'java.util.TreeMap','sm':{'foo':'bar'},'@vtype':'java.util.TreeMap','m':{'foo':'bar'}," +
                        "'keyType':'java.lang.String','valueType':'java.lang.String'}"));

        TestingUtils.testFully(gsonAllDuplicatesMode, obj, jsonStr(
                "{'@vtype':'java.util.TreeMap','sm':{'foo':'bar'},'@vtype':'java.util.TreeMap','m':'@root.sm'," +
                        "'keyType':'java.lang.String','valueType':'@root.keyType'}"));

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
        Map<String, String> obj = new LinkedHashTreeMap<String, String>(MY_STRING_CMP);
        obj.put("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'foo':'bar','@.comparator':{'@type':'am.yagson.TestCustomMaps$1','@val':{}}}"));
    }

    public void testConcurrentHashMap() {
        Map<String, String> obj = new ConcurrentHashMap<String, String>();
        obj.put("foo", "bar");

        TestingUtils.testFully(obj, jsonStr(
                "{'foo':'bar'}"));
    }

    public void testEnumMap() {
        Map<TypeInfoPolicy, String> obj = new EnumMap<TypeInfoPolicy, String>(TypeInfoPolicy.class);
        obj.put(TypeInfoPolicy.DISABLED, "off");

        TestingUtils.testFully(obj, jsonStr(
                "[[{'@type':'am.yagson.types.TypeInfoPolicy','@val':'DISABLED'},'off']]"));

        TestingUtils.testFully(obj, new TypeToken<EnumMap<TypeInfoPolicy, String>>(){}, jsonStr(
                "{'DISABLED':'off'}"));
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
                "{'@vtype':'java.util.HashMap','map':[[{'@type':'java.util.jar.Attributes$Name'," +
                        "'@val':{'name':'foo','hashCode':'@hash'}},'bar']]}"));
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
