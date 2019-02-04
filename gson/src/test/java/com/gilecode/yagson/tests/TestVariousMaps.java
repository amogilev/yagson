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

import com.gilecode.yagson.YaGson;
import com.gilecode.yagson.tests.data.ClassWithObject;
import com.gilecode.yagson.tests.data.Person;
import com.gilecode.yagson.tests.util.BindingTestCase;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.JavaVersion;
import com.google.gson.internal.LinkedHashTreeMap;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.jar.Attributes;

/**
 * Tests serialization of various standard {@link Map}s from the Java Collections Framework.
 *
 * @author Andrey Mogilev
 */
public class TestVariousMaps extends BindingTestCase {

    private static Map<String, String> foobar = new LinkedHashMap<String, String>();
    static {
        foobar.put("foo", "bar");
    }


    public void testHashMap() {
        test(new HashMap<String, String>(foobar), jsonStr(
                "{'foo':'bar'}"));
        testAsMap(HashMap.class);
    }

    public void testTreeMap() {
        test(new TreeMap<String, String>(foobar), jsonStr(
                "{'foo':'bar'}"));
        testAsMap(TreeMap.class);
    }

    public void testTreeMapWithComparator() {
        SortedMap<String, String> map;

        map = new TreeMap<String, String>(MY_STRING_CMP);
        map.put("foo", "bar");
        test(map, jsonStr("{'foo':'bar','@.comparator':{'@type':'com.gilecode.yagson.tests.util.BindingTestCase$1','@val':{}}}"));

        map = new TreeMap<String, String>(MY_STRING_CMP);
        test(map, jsonStr("{'@.comparator':{'@type':'com.gilecode.yagson.tests.util.BindingTestCase$1','@val':{}}}"));

        map.putAll(mapsTestsElements);
        testAsMap(map);
    }

    public void testComplexTreeMapWithComparator() {
        SortedMap<Person, String> map = new TreeMap<Person, String>(MY_PERSON_CMP);

        map.put(new Person("Jane", "McDoe"), "bar");
        map.put(new Person("John", "Doe"), "foo");

        assertTrue(map.get(map.firstKey()).equals("foo"));

        SortedMap<Person, String> obj = map;
        test(obj, new TypeToken<TreeMap<Person, String>>(){}.getType(), jsonStr(
                "[" +
                        "[{'name':'John','family':'Doe'},'foo']," +
                        "[{'name':'Jane','family':'McDoe'},'bar']," +
                        "{'@.comparator':{'@type':'com.gilecode.yagson.tests.util.BindingTestCase$2','@val':{}}}" +
                        "]"));
    }

    public void testDeserializeComplexTreeMapWithIncorrectExtraFields() {
        try {
            new YaGson().fromJson(jsonStr(
                            "[" +
                                    "[{'name':'John','family':'Doe'},'foo']," +
                                    "[{'name':'Jane','family':'McDoe'},'bar']," +
                                    "{'@.comparator':{'@type':'com.gilecode.yagson.tests.util.BindingTestCase$2','@val':{}}," +
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
        Map<String, String> obj = Collections.unmodifiableMap(new HashMap<String, String>(mapsTestsElements));
        testAsMap(obj);
    }

    public void testUnmodifiableMap2() {
        Map<String, String> obj = Collections.unmodifiableMap(new HashMap<String, String>(foobar));
        test(obj, Map.class, jsonStr(
                "{'@type':'java.util.Collections$UnmodifiableMap','@val':{" +
                        "'m':{'@type':'java.util.HashMap','@val':{'foo':'bar'}}}}"));
    }

    public void testUnmodifiableMapInObject() {
        Map<Object, Object> umap = Collections.unmodifiableMap(new HashMap<Object, Object>(foobar));
        ClassWithObject obj = new ClassWithObject(umap);

        test(obj, jsonStr(
                "{'obj':{'@type':'java.util.Collections$UnmodifiableMap','@val':{" +
                        "'m':{'@type':'java.util.HashMap','@val':{'foo':'bar'}}}}}"));
    }

    public void testEmptyMap() {
        Map<String, String> obj = Collections.emptyMap();

        test(obj, jsonStr("{}"));
        testAsMap(obj);
    }

    public void testSynchronizedMap() {
        Map<String, String> obj1 = Collections.synchronizedMap(new HashMap<String, String>(foobar));
        Map<String, String> obj2 = Collections.synchronizedMap(new HashMap<String, String>(mapsTestsElements));

        test(obj1, jsonStr(
                "{'m':{'@type':'java.util.HashMap','@val':{'foo':'bar'}},'mutex':'@root'}"));
        testAsMap(obj2);
    }

    public void testSynchronizedSortedMap() {
        SortedMap<String, String> obj = Collections.synchronizedSortedMap(new TreeMap<String, String>(foobar));

        // NOTE: duplication in circular-only refs mode
        test(gsonCircularOnlyMode, obj, jsonStr(
                "{'sm':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}}," +
                        "'m':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}}," +
                        "'mutex':'@root'}"));

        test(gsonCircularAndSiblingsMode, obj, jsonStr(
                "{'sm':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}},'m':'@.sm','mutex':'@root'}"));

        test(obj, jsonStr(
                "{'sm':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}},'m':'@.sm','mutex':'@root'}"));
    }

    public void testCheckedMap() {
        Map<String, String> obj1 = Collections.checkedMap(new HashMap<String, String>(foobar),
                String.class, String.class);
        Map<String, String> obj2 = Collections.checkedMap(new HashMap<String, String>(mapsTestsElements),
                String.class, String.class);

        test(obj1, jsonStr(
                "{'m':{'@type':'java.util.HashMap','@val':{'foo':'bar'}}," +
                        "'keyType':'java.lang.String','valueType':'java.lang.String'}"));
        testAsMap(obj2);
    }

    public void testCheckedSortedMap() {
        Map<String, String> obj = Collections.checkedSortedMap(new TreeMap<String, String>(foobar),
                String.class, String.class);

        // NOTE: duplication in circular-only refs mode
        test(gsonCircularOnlyMode, obj, jsonStr(
                "{'sm':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}}," +
                        "'m':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}}," +
                        "'keyType':'java.lang.String','valueType':'java.lang.String'}"));

        test(gsonAllDuplicatesMode, obj, jsonStr(
                "{'sm':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}},'m':'@.sm'," +
                        "'keyType':'java.lang.String','valueType':'java.lang.String'}"));

        test(gsonAllDuplicatesMode, obj, jsonStr(
                "{'sm':{'@type':'java.util.TreeMap','@val':{'foo':'bar'}},'m':'@.sm'," +
                        "'keyType':'java.lang.String','valueType':'java.lang.String'}"));

    }

    public void testSingletonMap() {
        Map<String, String> obj = Collections.singletonMap("foo", "bar");

        test(obj, jsonStr(
                "{'k':'foo','v':'bar'}"));
        testAsMap(obj);
    }

    public void testLinkedHashMap() {
        Map<String, String> obj = new LinkedHashMap<String, String>(foobar);

        test(obj, jsonStr("{'foo':'bar'}"));
        testAsMap(LinkedHashMap.class);
    }

    public void testLinkedHashTreeMap() {
        testAsMap(LinkedHashTreeMap.class);
    }

    public void testLinkedHashTreeMapWithComparator() {
        Map<String, String> obj = new LinkedHashTreeMap<String, String>(MY_STRING_CMP);
        obj.put("foo", "bar");

        test(obj, jsonStr(
                "{'foo':'bar','@.comparator':{'@type':'com.gilecode.yagson.tests.util.BindingTestCase$1','@val':{}}}"));
    }

    public void testConcurrentHashMap() {
        Map<String, String> obj = new ConcurrentHashMap<String, String>(foobar);

        test(obj, jsonStr("{'foo':'bar'}"));
        testAsMap(ConcurrentHashMap.class);
    }

    public void testConcurrentSkipListMap() {
        Map<String, String> obj = new ConcurrentSkipListMap<String, String>(foobar);

        test(obj, jsonStr("{'foo':'bar'}"));
        testAsMap(ConcurrentSkipListMap.class);
    }

    public void testHashtable() {
        testAsMap(Hashtable.class);
    }

    public void testProperties() {
        Map<Object, Object> obj = new Properties();
        obj.put("foo", "bar");

        test(obj, jsonStr("{'foo':'bar'}"));
        testAsMap(Properties.class);
    }

    public void testPropertiesWithDefaults() {
        Properties defaults = new Properties();
        defaults.put("foo0", "defaultFoo0");

        Properties obj = new Properties(defaults);
        obj.put("foo", "bar");

        Properties result = test(obj, jsonStr(
                "{'foo':'bar','@.defaults':{'foo0':'defaultFoo0'}}"));
        assertEquals("defaultFoo0", result.getProperty("foo0"));
    }

    public void testAttributes() {
        Attributes obj = new Attributes();
        obj.putValue("foo", "bar");

        if (JavaVersion.isJava9OrLater()) {
            test(obj, jsonStr(
                    "{'map':{'@type':'java.util.LinkedHashMap','@val':[[" +
                            "{'@type':'java.util.jar.Attributes$Name','@val':{'name':'foo','hashCode':'@hash'}},'bar']]}}"));
        } else {
            test(obj, jsonStr(
                    "{'map':{'@type':'java.util.HashMap','@val':[[" +
                            "{'@type':'java.util.jar.Attributes$Name','@val':{'name':'foo','hashCode':'@hash'}},'bar']]}}"));
        }
        testAsMap(obj);
    }

    public void testWeakHashMap() {
        WeakHashMap<String, String> obj = new WeakHashMap<String, String>(foobar);

        test(obj, jsonStr("{'foo':'bar'}"));
        testAsMap(WeakHashMap.class);
    }

    public void testIdentityHashMap() {
        IdentityHashMap<String, String> obj = new IdentityHashMap<String, String>(foobar);

        test(obj, jsonStr("{'foo':'bar'}"));
        testAsMap(IdentityHashMap.class);
    }

    public void testDuplicateMapKeys(){
        HashMap<Integer, Double> first = new HashMap<Integer, Double>();
        first.put(1, 0.0);

        HashMap<Integer, Double> second = new HashMap<Integer, Double>();
        second.put(1, 0.0);

        List<HashMap<Integer, Double>> list = new ArrayList<HashMap<Integer, Double>>();
        list.add(first);
        list.add(second);

        Type type = new TypeToken<ArrayList<HashMap<Integer,Double>>>(){}.getType();
        test(list, type, jsonStr("[{'1':0.0},{'1':0.0}]"));
    }

}
