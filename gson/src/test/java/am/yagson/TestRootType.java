package am.yagson;

import com.google.gson.Gson;
import junit.framework.TestCase;

import java.math.BigDecimal;
import java.util.*;

import static am.yagson.TestingUtils.jsonStr;

// TODO
public class TestRootType extends TestCase {

    private <T> void testWithRootType(T obj, Class<? super T> deserializationType, String expected) {
        YaGson gson = new YaGson();
        String json = gson.toJson(obj, deserializationType);
        assertEquals(expected, json);

        Object obj2 = gson.fromJson(json, deserializationType);
        if (obj == null) {
            assertNull(obj2);
        } else {
            assertEquals(obj.getClass(), obj2.getClass());
        }

        String json2 = gson.toJson(obj2, deserializationType);
        assertEquals(expected, json2);
    }

    public void testRootNull() {
        testWithRootType(null, Object.class, "null");
        testWithRootType(null, String.class, "null");
        testWithRootType(null, Integer.class, "null");
        testWithRootType(null, Person.class, "null");
        testWithRootType(null, List.class, "null");
        testWithRootType(null, Map.class, "null");
        testWithRootType(null, String[].class, "null");
    }

    public void testRootString() {
        testWithRootType("", Object.class, jsonStr("''"));
        testWithRootType("foo", Object.class, jsonStr("'foo'"));
        testWithRootType("foo", String.class, jsonStr("'foo'"));
    }

    public void testRootDouble() {
        testWithRootType(10.0, Object.class, "10.0");
        testWithRootType(10.0, Number.class, "10.0");
        testWithRootType(10.0, double.class, "10.0");
    }

    public void testRootLong() {
        testWithRootType(10L, Object.class, "10");
        testWithRootType(10L, Number.class, "10");
        testWithRootType(10L, long.class, "10");
    }

    public void testRootBoolean() {
        testWithRootType(false, Object.class, "false");
        testWithRootType(true, Object.class, "true");
        testWithRootType(false, Boolean.class, "false");
        testWithRootType(true, Boolean.class, "true");
        testWithRootType(false, boolean.class, "false");
        testWithRootType(true, boolean.class, "true");
    }

    public void testRootInt() {
        testWithRootType(10, Object.class, jsonStr(
                "{'@type':'java.lang.Integer','@val':10}"));
        testWithRootType(10, Number.class, jsonStr(
                "{'@type':'java.lang.Integer','@val':10}"));
        testWithRootType(10, Integer.class, "10");
        testWithRootType(10, int.class, "10");
    }

    public void testRootFloat() {
        testWithRootType(10.0f, Object.class, jsonStr(
                "{'@type':'java.lang.Float','@val':10.0}"));
        testWithRootType(10.0f, Number.class, jsonStr(
                "{'@type':'java.lang.Float','@val':10.0}"));
        testWithRootType(10.0f, Float.class, "10.0");
        testWithRootType(10.0f, float.class, "10.0");
    }

    public void testRootBigDecimal() {
        BigDecimal num = BigDecimal.valueOf(2).pow(100);

        testWithRootType(num, Object.class, jsonStr(
                "{'@type':'java.math.BigDecimal','@val':1267650600228229401496703205376}"));
        testWithRootType(num, Number.class, jsonStr(
                "{'@type':'java.math.BigDecimal','@val':1267650600228229401496703205376}"));
        testWithRootType(num, BigDecimal.class, "1267650600228229401496703205376");
    }

    public void testRootPerson() {
        PersonEx obj = new PersonEx("foo", "bar", "addr");
        testWithRootType(obj, Object.class, jsonStr(
                "{'@type':'am.yagson.PersonEx','@val':{'address':'addr','name':'foo','family':'bar'}}"));
        testWithRootType(obj, Person.class, jsonStr(
                "{'@type':'am.yagson.PersonEx','@val':{'address':'addr','name':'foo','family':'bar'}}"));
        testWithRootType(obj, PersonEx.class, jsonStr(
                "{'address':'addr','name':'foo','family':'bar'}"));
    }

    public void testRootArrayList() {
        ArrayList<String> l = new ArrayList<String>();
        l.add("foo");
        testWithRootType(l, Object.class, jsonStr(
                "['foo']"));
        testWithRootType(l, Collection.class, jsonStr(
                "['foo']"));
        testWithRootType(l, List.class, jsonStr(
                "['foo']"));
        testWithRootType(l, ArrayList.class, jsonStr(
                "['foo']"));
        // AbstractCollection case is not processed in defaults now, so root type info is emitted
        testWithRootType(l, AbstractCollection.class, jsonStr(
                "{'@type':'java.util.ArrayList','@val':['foo']}"));
    }

    public void testRootLinkedList() {
        LinkedList<String> l = new LinkedList<String>();
        l.add("foo");
        testWithRootType(l, Object.class, jsonStr(
                "{'@type':'java.util.LinkedList','@val':['foo']}"));
        testWithRootType(l, Collection.class, jsonStr(
                "{'@type':'java.util.LinkedList','@val':['foo']}"));
        testWithRootType(l, List.class, jsonStr(
                "{'@type':'java.util.LinkedList','@val':['foo']}"));
        testWithRootType(l, AbstractCollection.class, jsonStr(
                "{'@type':'java.util.LinkedList','@val':['foo']}"));
        testWithRootType(l, LinkedList.class, jsonStr(
                "['foo']"));
    }

    public void testRootMap() {
        HashMap<Number, String> m = new HashMap<Number, String>();
        m.put(1L, "long");
        testWithRootType(m, Object.class, jsonStr(
                "{'@type':'java.util.HashMap','@val':{'1':'long'}}"));
        testWithRootType(m, Map.class, jsonStr(
                "{'@type':'java.util.HashMap','@val':{'1':'long'}}"));
        testWithRootType(m, HashMap.class, jsonStr(
                "{'1':'long'}"));
    }

    public void testRootStringArray() {
        String[] arr = new String[]{"foo", "bar"};
        testWithRootType(arr, Object.class, jsonStr(
                "{'@type':'[Ljava.lang.String;','@val':['foo','bar']}"));
        testWithRootType(arr, Object[].class, jsonStr(
                "{'@type':'[Ljava.lang.String;','@val':['foo','bar']}"));
        testWithRootType(arr, String[].class, jsonStr(
                "['foo','bar']"));
    }

    public void testRootObjectArray() {
        Object[] arr = new Object[]{"foo", 1L};
        testWithRootType(arr, Object.class, jsonStr(
                "{'@type':'[Ljava.lang.Object;','@val':['foo',1]}"));
        testWithRootType(arr, Object[].class, jsonStr(
                "['foo',1]"));
    }
}
