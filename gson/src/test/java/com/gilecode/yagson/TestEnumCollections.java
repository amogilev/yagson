package com.gilecode.yagson;

import com.gilecode.yagson.types.TypeInfoPolicy;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.gilecode.yagson.TestingUtils.jsonStr;

public class TestEnumCollections extends TestCase {

    public void testEnumSet1() {
        EnumSet<TypeInfoPolicy> obj = EnumSet.of(TypeInfoPolicy.DISABLED);

        TestingUtils.testFully(obj, jsonStr(
                "{'@type':'java.util.EnumSet<com.gilecode.yagson.types.TypeInfoPolicy>','@val':['DISABLED']}"));
    }

    public void testEnumSet2() {

        Object obj1 = new EmptyClass() {
            EnumSet<TypeInfoPolicy> set = EnumSet.of(TypeInfoPolicy.DISABLED);
            public String toString() {
                return "EmptyClass{" + "set=" + set + "}";
            }
        };

        TestingUtils.testFullyByToString(obj1, jsonStr(
                "{'set':{'@type':'java.util.EnumSet<com.gilecode.yagson.types.TypeInfoPolicy>','@val':['DISABLED']}," +
                        "'this$0':{'fName':'testEnumSet2'}}"));

        Object obj2 = new EmptyClass() {
            EnumSet set = EnumSet.of(TypeInfoPolicy.DISABLED);
            public String toString() {
                return "EmptyClass{" + "set=" + set + "}";
            }
        };

        TestingUtils.testFullyByToString(obj2, jsonStr(
                "{'set':{'@type':'java.util.EnumSet<com.gilecode.yagson.types.TypeInfoPolicy>','@val':['DISABLED']}," +
                        "'this$0':{'fName':'testEnumSet2'}}"));
    }

    public void testEnumSetWithNoTypeInfo() throws Exception {
        EnumSet<TypeInfoPolicy> obj = EnumSet.of(TypeInfoPolicy.DISABLED);

        TestingUtils.testFully(obj, new TypeToken<EnumSet<TypeInfoPolicy>>() {}, jsonStr(
                "['DISABLED']"));
    }

    public void testEnumSetWithInheritance1() {
        EnumSet<TimeUnit> obj = EnumSet.of(TimeUnit.SECONDS);

        TestingUtils.testFully(obj, jsonStr(
                "{'@type':'java.util.EnumSet<java.util.concurrent.TimeUnit>'," +
                        "'@val':['SECONDS']}"));

        TestingUtils.testFully(obj, new TypeToken<EnumSet<TimeUnit>>() {}, jsonStr(
                "['SECONDS']"));
    }

    public void testRegularEnumSet() throws Exception {
        EnumSet<TypeInfoPolicy> obj = EnumSet.of(TypeInfoPolicy.DISABLED);

        TypeToken<?> typeToken = TypeToken.get($Gson$Types.newParameterizedTypeWithOwner(null,
                Class.forName("java.util.RegularEnumSet"), TypeInfoPolicy.class));
        TestingUtils.testFully(obj, typeToken, jsonStr(
                "['DISABLED']"));
    }

    public void testEmptyEnumSet() {
        EnumSet<TypeInfoPolicy> obj = EnumSet.noneOf(TypeInfoPolicy.class);

        EnumSet<TypeInfoPolicy> result = TestingUtils.testFully(obj, jsonStr(
                "{'@type':'java.util.EnumSet<com.gilecode.yagson.types.TypeInfoPolicy>','@val':[]}"));
        result.add(TypeInfoPolicy.DISABLED);

        TestingUtils.testFully(obj, new TypeToken<EnumSet<TypeInfoPolicy>>() {}, jsonStr(
                "[]"));
    }

    public void testEnumMap() {
        Map<TypeInfoPolicy, String> obj = new EnumMap<TypeInfoPolicy, String>(TypeInfoPolicy.class);
        obj.put(TypeInfoPolicy.DISABLED, "off");

        TestingUtils.testFully(obj, jsonStr(
                "{'@type':'java.util.EnumMap<com.gilecode.yagson.types.TypeInfoPolicy,?>','@val':{'DISABLED':'off'}}"));

        TestingUtils.testFully(obj, new TypeToken<EnumMap<TypeInfoPolicy, String>>(){}, jsonStr(
                "{'DISABLED':'off'}"));
    }

    public void testEmptyEnumMap() {
        Map<TypeInfoPolicy, String> obj = new EnumMap<TypeInfoPolicy, String>(TypeInfoPolicy.class);

        TestingUtils.testFully(obj, jsonStr(
                "{'@type':'java.util.EnumMap<com.gilecode.yagson.types.TypeInfoPolicy,?>','@val':{}}"));

        TestingUtils.testFully(obj, new TypeToken<EnumMap<TypeInfoPolicy, String>>(){}, jsonStr(
                "{}"));
    }

    abstract class MyEnumMap1 extends EnumMap<TimeUnit, String>  {
        public MyEnumMap1(String unused) {
            super(TimeUnit.class);
        }
    }

    class MyEnumMap2 extends MyEnumMap1  {
        public MyEnumMap2() {
            super("unused");
        }
    }

    class MyEnumMap4<V> extends EnumMap<TypeInfoPolicy,V>  {
        public MyEnumMap4(String dummy) {
            super(TypeInfoPolicy.class);
        }
    }

    class MyEnumMap5<T extends Enum<T>> extends EnumMap<T, String>  {
        public MyEnumMap5(Class<T> enumClass) {
            super(enumClass);
        }
    }

    public void testMyEnumMap() {
        MyEnumMap2 obj = new MyEnumMap2();
        obj.put(TimeUnit.DAYS, "days");

        TestingUtils.testFully(obj, MyEnumMap2.class, jsonStr(
                "{'DAYS':'days'}"));

        TestingUtils.testFully(obj, MyEnumMap1.class, jsonStr(
                "{'@type':'com.gilecode.yagson.TestEnumCollections$MyEnumMap2','@val':{'DAYS':'days'}}"));
    }

    public void testMyEnumMap4() {
        MyEnumMap4 obj = new MyEnumMap4("");
        obj.put(TypeInfoPolicy.DISABLED, "foo");


        obj = TestingUtils.testFully(obj, new TypeToken<EnumMap<TypeInfoPolicy, String>>(){}, jsonStr(
                "{'@type':'com.gilecode.yagson.TestEnumCollections$MyEnumMap4','@val':{'DISABLED':'foo'}}"));

        TestingUtils.testFully(obj, MyEnumMap4.class, jsonStr(
                "{'DISABLED':'foo'}"));
    }

    public void testMyEnumMap5() {
        MyEnumMap5 obj = new MyEnumMap5(TypeInfoPolicy.class);
        obj.put(TypeInfoPolicy.DISABLED, "foo");


        obj = TestingUtils.testFully(obj, new TypeToken<EnumMap<TypeInfoPolicy, String>>(){}, jsonStr(
                "{'@type':'com.gilecode.yagson.TestEnumCollections$MyEnumMap5<com.gilecode.yagson.types.TypeInfoPolicy>','@val':{'DISABLED':'foo'}}"));

        TestingUtils.testFully(obj, MyEnumMap5.class, jsonStr(
                "{'@type':'com.gilecode.yagson.TestEnumCollections$MyEnumMap5<com.gilecode.yagson.types.TypeInfoPolicy>','@val':{'DISABLED':'foo'}}"));

        TestingUtils.testFully(obj, new TypeToken<MyEnumMap5<TypeInfoPolicy>>(){}, jsonStr(
                "{'DISABLED':'foo'}"));

    }




}
