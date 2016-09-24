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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Tests for arrays, collections and maps with elements of mixed types.
 *
 * @author Andrey Mogilev
 */
public class TestMixedTypesCollections extends BindingTestCase {

    //
    // mixed arrays
    //

    public void testMixedNumbersInNumberArray() {
        Number[] arr = new Number[]{null, 1, 2L, 3.01f, 4.02, BigDecimal.ONE};
        test(arr, jsonStr("[null," +
                "{'@type':'java.lang.Integer','@val':1}," +
                "2," +
                "{'@type':'java.lang.Float','@val':3.01}," +
                "4.02," +
                "{'@type':'java.math.BigDecimal','@val':1}]"));
    }

    public void testMixedNumbersInObjectArray() {
        Object[] arr = new Object[]{null, 1, 2L, 3.01f, 4.02, BigDecimal.ONE};
        test(arr, jsonStr("[null," +
                "{'@type':'java.lang.Integer','@val':1}," +
                "2," +
                "{'@type':'java.lang.Float','@val':3.01}," +
                "4.02," +
                "{'@type':'java.math.BigDecimal','@val':1}]"));
    }

    public void testMixedPersonArray() {
        Person[] arr = new Person[]{new Person("John", "Doe"), new PersonEx("Jane", "Doe", "addr")};

        test(arr, jsonStr("[" +
                "{'name':'John','family':'Doe'}," +
                "{'@type':'com.gilecode.yagson.tests.data.PersonEx','@val':{'address':'addr','name':'Jane','family':'Doe'}}]"));
    }

    //
    // mixed maps
    //

    public void testMixedComplexKeyMap() {
        Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put(new Person("John", "Doe"), "M");
        map.put(new PersonEx("Jane", "Doe", "addr"), "F");

        test(map, jsonStr(
                "[[{'@type':'com.gilecode.yagson.tests.data.Person','@val':{'name':'John','family':'Doe'}},'M']," +
                "[{'@type':'com.gilecode.yagson.tests.data.PersonEx','@val':{'address':'addr','name':'Jane','family':'Doe'}},'F']]"));
    }

    public void testMixedNumbersMap() {
        Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put(1, "int");
        map.put(2L, "long");
        map.put(BigDecimal.ONE, 1);

        test(map, jsonStr(
                "[[{'@type':'java.lang.Integer','@val':1},'int']," +
                "[{'@type':'java.lang.Long','@val':2},'long']," +
                "[{'@type':'java.math.BigDecimal','@val':1},{'@type':'java.lang.Integer','@val':1}]]"));
    }

    //
    // mixed collections
    //

    public void testMixedNumbersCollection() {
        List<Object> obj = new ArrayList<Object>(asList(1, 2L, 3.01f, 4.02, BigDecimal.ONE));
        test(obj, jsonStr("[" +
                "{'@type':'java.lang.Integer','@val':1}," +
                "2," +
                "{'@type':'java.lang.Float','@val':3.01}," +
                "4.02," +
                "{'@type':'java.math.BigDecimal','@val':1}]"));
    }

    public void testMixedPersonCollection() {
        List<Object> obj = new ArrayList<Object>();
        obj.add(new Person("John", "Doe"));
        obj.add(new PersonEx("Jane", "Doe", "addr"));

        test(obj, jsonStr("[" +
                "{'@type':'com.gilecode.yagson.tests.data.Person','@val':{'name':'John','family':'Doe'}}," +
                "{'@type':'com.gilecode.yagson.tests.data.PersonEx','@val':{'address':'addr','name':'Jane','family':'Doe'}}]"));
    }

}
