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

import com.gilecode.yagson.tests.util.BindingTestCase;
import com.gilecode.yagson.tests.util.EqualityCheckMode;
import com.gilecode.yagson.types.TypeInfoPolicy;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Tests a lot of various {@link EnumSet}s and {@link EnumMap}s.
 *
 * @author Andrey Mogilev
 */
public class TestEnumCollections extends BindingTestCase {

    private static final Type ENUMSET_TIP_TYPE = new TypeToken<EnumSet<TypeInfoPolicy>>() {}.getType();
    private static final Type ENUMMAP_TIP_TYPE = new TypeToken<EnumMap<TypeInfoPolicy, String>>(){}.getType();

    public void testEnumSet1() {
        EnumSet<TypeInfoPolicy> obj = EnumSet.of(TypeInfoPolicy.DISABLED);

        test(obj, jsonStr(
                "{'@type':'java.util.EnumSet<com.gilecode.yagson.types.TypeInfoPolicy>','@val':['DISABLED']}"));
        testAsCollection(obj);
    }

    public void testEnumSet2() {

        Object obj1 = new Object() {
            EnumSet<TypeInfoPolicy> set = EnumSet.of(TypeInfoPolicy.DISABLED);
            public String toString() {
                return "{" + "set=" + set + "}";
            }
        };

        test(obj1, jsonStr(
                "{'set':{'@type':'java.util.EnumSet<com.gilecode.yagson.types.TypeInfoPolicy>','@val':['DISABLED']}," +
                        "'this$0':{'fName':'testEnumSet2'}}"),
                EqualityCheckMode.TO_STRING);
    }

    public void testEnumSetWithNoTypeInfo() throws Exception {
        EnumSet<TypeInfoPolicy> obj = EnumSet.of(TypeInfoPolicy.DISABLED);

        test(obj, ENUMSET_TIP_TYPE, jsonStr(
                "['DISABLED']"));
    }

    public void testEnumSetWithInheritance1() {
        EnumSet<TimeUnit> obj = EnumSet.of(TimeUnit.SECONDS);

        test(obj, jsonStr(
                "{'@type':'java.util.EnumSet<java.util.concurrent.TimeUnit>'," +
                        "'@val':['SECONDS']}"));

        test(obj, new TypeToken<EnumSet<TimeUnit>>(){}.getType(), jsonStr(
                "['SECONDS']"));
    }

    public void testRegularEnumSet() throws Exception {
        EnumSet<TypeInfoPolicy> obj = EnumSet.of(TypeInfoPolicy.DISABLED);

        TypeToken<?> typeToken = TypeToken.get($Gson$Types.newParameterizedTypeWithOwner(null,
                Class.forName("java.util.RegularEnumSet"), TypeInfoPolicy.class));
        test(obj, typeToken.getType(), jsonStr(
                "['DISABLED']"));
    }

    public void testEmptyEnumSet() {
        EnumSet<TypeInfoPolicy> obj = EnumSet.noneOf(TypeInfoPolicy.class);

        EnumSet<TypeInfoPolicy> result = test(obj, jsonStr(
                "{'@type':'java.util.EnumSet<com.gilecode.yagson.types.TypeInfoPolicy>','@val':[]}"));
        result.add(TypeInfoPolicy.DISABLED);

        test(obj, ENUMSET_TIP_TYPE, jsonStr(
                "[]"));
    }

    public void testEnumMap() {
        Map<TypeInfoPolicy, String> obj = new EnumMap<TypeInfoPolicy, String>(TypeInfoPolicy.class);
        obj.put(TypeInfoPolicy.DISABLED, "off");

        test(obj, jsonStr(
                "{'@type':'java.util.EnumMap<com.gilecode.yagson.types.TypeInfoPolicy,?>','@val':{'DISABLED':'off'}}"));

        test(obj, ENUMMAP_TIP_TYPE , jsonStr(
                "{'DISABLED':'off'}"));

        testAsMap(obj);
    }

    public void testEmptyEnumMap() {
        Map<TypeInfoPolicy, String> obj = new EnumMap<TypeInfoPolicy, String>(TypeInfoPolicy.class);

        test(obj, jsonStr(
                "{'@type':'java.util.EnumMap<com.gilecode.yagson.types.TypeInfoPolicy,?>','@val':{}}"));

        test(obj, ENUMMAP_TIP_TYPE, jsonStr(
                "{}"));
    }

    private abstract class MyEnumMap1 extends EnumMap<TimeUnit, String>  {
        MyEnumMap1(String unused) {
            super(TimeUnit.class);
        }
    }

    private class MyEnumMap2 extends MyEnumMap1  {
        MyEnumMap2() {
            super("unused");
        }
    }

    private class MyEnumMap4<V> extends EnumMap<TypeInfoPolicy,V>  {
        MyEnumMap4(String dummy) {
            super(TypeInfoPolicy.class);
        }
    }

    private class MyEnumMap5<T extends Enum<T>> extends EnumMap<T, String>  {
        MyEnumMap5(Class<T> enumClass) {
            super(enumClass);
        }
    }

    public void testMyEnumMap() {
        MyEnumMap2 obj = new MyEnumMap2();
        obj.put(TimeUnit.DAYS, "days");

        test(obj, MyEnumMap2.class, jsonStr(
                "{'DAYS':'days'}"));

        test(obj, MyEnumMap1.class, jsonStr(
                "{'@type':'com.gilecode.yagson.tests.TestEnumCollections$MyEnumMap2','@val':{'DAYS':'days'}}"));

        testAsMap(obj);
    }

    public void testMyEnumMap4() {
        MyEnumMap4<String> obj = new MyEnumMap4<String>("");
        obj.put(TypeInfoPolicy.DISABLED, "foo");


        obj = test(obj, ENUMMAP_TIP_TYPE, jsonStr(
                "{'@type':'com.gilecode.yagson.tests.TestEnumCollections$MyEnumMap4','@val':{'DISABLED':'foo'}}"));

        test(obj, MyEnumMap4.class, jsonStr(
                "{'DISABLED':'foo'}"));
    }

    public void testMyEnumMap5() {
        MyEnumMap5<TypeInfoPolicy> obj = new MyEnumMap5<TypeInfoPolicy>(TypeInfoPolicy.class);
        obj.put(TypeInfoPolicy.DISABLED, "foo");


        obj = test(obj, ENUMMAP_TIP_TYPE, jsonStr(
                "{'@type':'com.gilecode.yagson.tests.TestEnumCollections$MyEnumMap5<com.gilecode.yagson.types.TypeInfoPolicy>','@val':{'DISABLED':'foo'}}"));

        test(obj, MyEnumMap5.class, jsonStr(
                "{'@type':'com.gilecode.yagson.tests.TestEnumCollections$MyEnumMap5<com.gilecode.yagson.types.TypeInfoPolicy>','@val':{'DISABLED':'foo'}}"));

        test(obj, new TypeToken<MyEnumMap5<TypeInfoPolicy>>(){}.getType(), jsonStr(
                "{'DISABLED':'foo'}"));
    }
}
