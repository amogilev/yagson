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
import com.gilecode.yagson.tests.data.PersonEx;
import com.gilecode.yagson.tests.util.BindingTestCase;
import com.gilecode.yagson.types.TypeInfoPolicy;
import com.google.gson.reflect.TypeToken;

import java.math.BigDecimal;
import java.util.*;

/**
 * Tests serialization of various primitive and simple objects at root level, with various deserialization types.
 *
 * @author Andrey Mogilev
 */
public class TestRootType extends BindingTestCase {

    public void testRootNull() {
        test((Object) null, Object.class, "null");
        test((String) null, String.class, "null");
        test((Integer) null, Integer.class, "null");
        test((Person) null, Person.class, "null");
        test((List) null, List.class, "null");
        test((Map) null, Map.class, "null");
        test((String[]) null, String[].class, "null");
    }

    public void testRootString() {
        test("", Object.class, jsonStr("''"));
        test("foo", Object.class, jsonStr("'foo'"));
        test("foo", String.class, jsonStr("'foo'"));
    }

    public void testRootDouble() {
        test(10.0, Object.class, "10.0");
        test(10.0, Number.class, "10.0");
        test(10.0, double.class, "10.0");
    }

    public void testRootLong() {
        test(10L, Object.class, "10");
        test(10L, Number.class, "10");
        test(10L, long.class, "10");
    }

    public void testRootBoolean() {
        test(false, Object.class, "false");
        test(true, Object.class, "true");
        test(false, Boolean.class, "false");
        test(true, Boolean.class, "true");
        test(false, boolean.class, "false");
        test(true, boolean.class, "true");
    }

    public void testRootInt() {
        test(10, Object.class, jsonStr(
                "{'@type':'java.lang.Integer','@val':10}"));
        test(10, Number.class, jsonStr(
                "{'@type':'java.lang.Integer','@val':10}"));
        test(10, Integer.class, "10");
        test(10, int.class, "10");
    }

    public void testRootFloat() {
        test(10.0f, Object.class, jsonStr(
                "{'@type':'java.lang.Float','@val':10.0}"));
        test(10.0f, Number.class, jsonStr(
                "{'@type':'java.lang.Float','@val':10.0}"));
        test(10.0f, Float.class, "10.0");
        test(10.0f, float.class, "10.0");
    }

    public void testRootBigDecimal() {
        BigDecimal num = BigDecimal.valueOf(2).pow(100);

        test(num, Object.class, jsonStr(
                "{'@type':'java.math.BigDecimal','@val':1267650600228229401496703205376}"));
        test(num, Number.class, jsonStr(
                "{'@type':'java.math.BigDecimal','@val':1267650600228229401496703205376}"));
        test(num, BigDecimal.class, "1267650600228229401496703205376");
    }

    public void testRootPerson() {
        PersonEx obj = new PersonEx("foo", "bar", "addr");
        test(obj, Object.class, jsonStr(
                "{'@type':'com.gilecode.yagson.tests.data.PersonEx','@val':{'address':'addr','name':'foo','family':'bar'}}"));
        test(obj, Person.class, jsonStr(
                "{'@type':'com.gilecode.yagson.tests.data.PersonEx','@val':{'address':'addr','name':'foo','family':'bar'}}"));
        test(obj, PersonEx.class, jsonStr(
                "{'address':'addr','name':'foo','family':'bar'}"));
    }

    public void testRootArrayList() {
        ArrayList<String> l = new ArrayList<String>();
        l.add("foo");
        test(l, Object.class, jsonStr(
                "['foo']"));
        test(l, Collection.class, jsonStr(
                "['foo']"));
        test(l, List.class, jsonStr(
                "['foo']"));
        test(l, ArrayList.class, jsonStr(
                "['foo']"));
        // AbstractCollection case is not processed in defaults now, so root type info is emitted
        test(l, AbstractCollection.class, jsonStr(
                "{'@type':'java.util.ArrayList','@val':['foo']}"));
    }

    public void testRootLinkedList() {
        LinkedList<String> l = new LinkedList<String>();
        l.add("foo");
        test(l, Object.class, jsonStr(
                "{'@type':'java.util.LinkedList','@val':['foo']}"));
        test(l, Collection.class, jsonStr(
                "{'@type':'java.util.LinkedList','@val':['foo']}"));
        test(l, List.class, jsonStr(
                "{'@type':'java.util.LinkedList','@val':['foo']}"));
        test(l, AbstractCollection.class, jsonStr(
                "{'@type':'java.util.LinkedList','@val':['foo']}"));
        test(l, LinkedList.class, jsonStr(
                "['foo']"));
    }

    public void testRootMap1() {
        HashMap<Number, String> m = new HashMap<Number, String>();
        m.put(1L, "long");
        test(m, Object.class, jsonStr(
                "{'@type':'java.util.HashMap','@val':[[{'@type':'java.lang.Long','@val':1},'long']]}"));
        test(m, new TypeToken<Map<Number, String>>(){}.getType(), jsonStr(
                "{'@type':'java.util.HashMap','@val':{'1':'long'}}"));
        test(m, HashMap.class, jsonStr(
                "[[{'@type':'java.lang.Long','@val':1},'long']]"));
        test(m, new TypeToken<HashMap<Number, String>>(){}.getType(), jsonStr(
                "{'1':'long'}"));
    }

    public void testRootMap2() {
        HashMap<String, Number> m = new HashMap<String, Number>();
        m.put("foo", 1L);
        test(m, Object.class, jsonStr(
                "{'@type':'java.util.HashMap','@val':{'foo':1}}"));
        test(m, new TypeToken<Map<String, Number>>(){}.getType(), jsonStr(
                "{'@type':'java.util.HashMap','@val':{'foo':1}}"));
        test(m, HashMap.class, jsonStr(
                "{'foo':1}"));
        test(m, new TypeToken<HashMap<String, Number>>(){}.getType(), jsonStr(
                "{'foo':1}"));
    }

    public void testRootStringArray() {
        String[] arr = new String[]{"foo", "bar"};
        test(arr, Object.class, jsonStr(
                "{'@type':'[Ljava.lang.String;','@val':['foo','bar']}"));
        test(arr, Object[].class, jsonStr(
                "{'@type':'[Ljava.lang.String;','@val':['foo','bar']}"));
        test(arr, String[].class, jsonStr(
                "['foo','bar']"));
    }

    public void testRootObjectArray() {
        Object[] arr = new Object[]{"foo", 1L};
        test(arr, Object.class, jsonStr(
                "{'@type':'[Ljava.lang.Object;','@val':['foo',1]}"));
        test(arr, Object[].class, jsonStr(
                "['foo',1]"));
    }

    public void testRootEnum() {
        TypeInfoPolicy obj = TypeInfoPolicy.DISABLED;
        test(obj, Object.class, jsonStr(
                "{'@type':'com.gilecode.yagson.types.TypeInfoPolicy','@val':'DISABLED'}"));
        test(obj, TypeInfoPolicy.class, jsonStr(
                "'DISABLED'"));
    }
}
