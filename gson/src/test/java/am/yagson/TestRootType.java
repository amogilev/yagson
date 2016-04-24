package am.yagson;

import am.yagson.types.TypeInfoPolicy;
import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.math.BigDecimal;
import java.util.*;

import static am.yagson.TestingUtils.jsonStr;

public class TestRootType extends TestCase {

    public void testRootNull() {
        TestingUtils.testFully((Object) null, Object.class, "null");
        TestingUtils.testFully((String) null, String.class, "null");
        TestingUtils.testFully((Integer) null, Integer.class, "null");
        TestingUtils.testFully((Person) null, Person.class, "null");
        TestingUtils.testFully((List) null, List.class, "null");
        TestingUtils.testFully((Map) null, Map.class, "null");
        TestingUtils.testFully((String[]) null, String[].class, "null");
    }

    public void testRootString() {
        TestingUtils.testFully("", Object.class, jsonStr("''"));
        TestingUtils.testFully("foo", Object.class, jsonStr("'foo'"));
        TestingUtils.testFully("foo", String.class, jsonStr("'foo'"));
    }

    public void testRootDouble() {
        TestingUtils.testFully(10.0, Object.class, "10.0");
        TestingUtils.testFully(10.0, Number.class, "10.0");
        TestingUtils.testFully(10.0, double.class, "10.0");
    }

    public void testRootLong() {
        TestingUtils.testFully(10L, Object.class, "10");
        TestingUtils.testFully(10L, Number.class, "10");
        TestingUtils.testFully(10L, long.class, "10");
    }

    public void testRootBoolean() {
        TestingUtils.testFully(false, Object.class, "false");
        TestingUtils.testFully(true, Object.class, "true");
        TestingUtils.testFully(false, Boolean.class, "false");
        TestingUtils.testFully(true, Boolean.class, "true");
        TestingUtils.testFully(false, boolean.class, "false");
        TestingUtils.testFully(true, boolean.class, "true");
    }

    public void testRootInt() {
        TestingUtils.testFully(10, Object.class, jsonStr(
                "{'@type':'java.lang.Integer','@val':10}"));
        TestingUtils.testFully(10, Number.class, jsonStr(
                "{'@type':'java.lang.Integer','@val':10}"));
        TestingUtils.testFully(10, Integer.class, "10");
        TestingUtils.testFully(10, int.class, "10");
    }

    public void testRootFloat() {
        TestingUtils.testFully(10.0f, Object.class, jsonStr(
                "{'@type':'java.lang.Float','@val':10.0}"));
        TestingUtils.testFully(10.0f, Number.class, jsonStr(
                "{'@type':'java.lang.Float','@val':10.0}"));
        TestingUtils.testFully(10.0f, Float.class, "10.0");
        TestingUtils.testFully(10.0f, float.class, "10.0");
    }

    public void testRootBigDecimal() {
        BigDecimal num = BigDecimal.valueOf(2).pow(100);

        TestingUtils.testFully(num, Object.class, jsonStr(
                "{'@type':'java.math.BigDecimal','@val':1267650600228229401496703205376}"));
        TestingUtils.testFully(num, Number.class, jsonStr(
                "{'@type':'java.math.BigDecimal','@val':1267650600228229401496703205376}"));
        TestingUtils.testFully(num, BigDecimal.class, "1267650600228229401496703205376");
    }

    public void testRootPerson() {
        PersonEx obj = new PersonEx("foo", "bar", "addr");
        TestingUtils.testFully(obj, Object.class, jsonStr(
                "{'@type':'am.yagson.PersonEx','@val':{'address':'addr','name':'foo','family':'bar'}}"));
        TestingUtils.testFully(obj, Person.class, jsonStr(
                "{'@type':'am.yagson.PersonEx','@val':{'address':'addr','name':'foo','family':'bar'}}"));
        TestingUtils.testFully(obj, PersonEx.class, jsonStr(
                "{'address':'addr','name':'foo','family':'bar'}"));
    }

    public void testRootArrayList() {
        ArrayList<String> l = new ArrayList<String>();
        l.add("foo");
//        TestingUtils.testFully(l, Object.class, jsonStr(
//                "['foo']"));
//        TestingUtils.testFully(l, Collection.class, jsonStr(
//                "['foo']"));
//        TestingUtils.testFully(l, List.class, jsonStr(
//                "['foo']"));
//        TestingUtils.testFully(l, ArrayList.class, jsonStr(
//                "['foo']"));
        // AbstractCollection case is not processed in defaults now, so root type info is emitted
        TestingUtils.testFully(l, AbstractCollection.class, jsonStr(
                "{'@type':'java.util.ArrayList','@val':['foo']}"));
    }

    public void testRootLinkedList() {
        LinkedList<String> l = new LinkedList<String>();
        l.add("foo");
        TestingUtils.testFully(l, Object.class, jsonStr(
                "{'@type':'java.util.LinkedList','@val':['foo']}"));
        TestingUtils.testFully(l, Collection.class, jsonStr(
                "{'@type':'java.util.LinkedList','@val':['foo']}"));
        TestingUtils.testFully(l, List.class, jsonStr(
                "{'@type':'java.util.LinkedList','@val':['foo']}"));
        TestingUtils.testFully(l, AbstractCollection.class, jsonStr(
                "{'@type':'java.util.LinkedList','@val':['foo']}"));
        TestingUtils.testFully(l, LinkedList.class, jsonStr(
                "['foo']"));
    }

    public void testRootMap1() {
        HashMap<Number, String> m = new HashMap<Number, String>();
        m.put(1L, "long");
        TestingUtils.testFully(m, Object.class, jsonStr(
                "{'@type':'java.util.HashMap','@val':[[{'@type':'java.lang.Long','@val':1},'long']]}"));
        TestingUtils.testFully(m, new TypeToken<Map<Number, String>>(){}, jsonStr(
                "{'@type':'java.util.HashMap','@val':{'1':'long'}}"));
        TestingUtils.testFully(m, HashMap.class, jsonStr(
                "[[{'@type':'java.lang.Long','@val':1},'long']]"));
        TestingUtils.testFully(m, new TypeToken<HashMap<Number, String>>(){}, jsonStr(
                "{'1':'long'}"));
    }

    public void testRootMap2() {
        HashMap<String, Number> m = new HashMap<String, Number>();
        m.put("foo", 1L);
        TestingUtils.testFully(m, Object.class, jsonStr(
                "{'@type':'java.util.HashMap','@val':{'foo':1}}"));
        TestingUtils.testFully(m, new TypeToken<Map<String, Number>>(){}, jsonStr(
                "{'@type':'java.util.HashMap','@val':{'foo':1}}"));
        TestingUtils.testFully(m, HashMap.class, jsonStr(
                "{'foo':1}"));
        TestingUtils.testFully(m, new TypeToken<HashMap<String, Number>>(){}, jsonStr(
                "{'foo':1}"));
    }

    public void testRootStringArray() {
        String[] arr = new String[]{"foo", "bar"};
        TestingUtils.testFully(arr, Object.class, jsonStr(
                "{'@type':'[Ljava.lang.String;','@val':['foo','bar']}"));
        TestingUtils.testFully(arr, Object[].class, jsonStr(
                "{'@type':'[Ljava.lang.String;','@val':['foo','bar']}"));
        TestingUtils.testFully(arr, String[].class, jsonStr(
                "['foo','bar']"));
    }

    public void testRootObjectArray() {
        Object[] arr = new Object[]{"foo", 1L};
        TestingUtils.testFully(arr, Object.class, jsonStr(
                "{'@type':'[Ljava.lang.Object;','@val':['foo',1]}"));
        TestingUtils.testFully(arr, Object[].class, jsonStr(
                "['foo',1]"));
    }

    public void testRootEnum() {
        TypeInfoPolicy obj = TypeInfoPolicy.DISABLED;
        TestingUtils.testFully(obj, Object.class, jsonStr(
                "{'@type':'am.yagson.types.TypeInfoPolicy','@val':'DISABLED'}"));
        TestingUtils.testFully(obj, TypeInfoPolicy.class, jsonStr(
                "'DISABLED'"));
    }
}
