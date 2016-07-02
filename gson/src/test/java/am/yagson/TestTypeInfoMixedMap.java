package am.yagson;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static am.yagson.TestingUtils.jsonStr;

public class TestTypeInfoMixedMap extends TestCase {

    private ClassWithMixedMap objToTestWithEmptyMap() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        return new ClassWithMixedMap(map);
    }

    private ClassWithMixedMap objToTest() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "int");
        map.put(2L, "long");
        map.put(BigDecimal.ONE, 1);

        return new ClassWithMixedMap(map);
    }

    private ClassWithMixedMap objToTestComplexKey() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(new Person("John", "Doe"), "M");
        map.put(new Person("Jane", "Doe"), "F");

        return new ClassWithMixedMap(map);
    }

    public void testComplexKey() {
        TestingUtils.testFully(objToTestComplexKey(), jsonStr(
                "{'map':{'@type':'java.util.HashMap','@val':[[" +
                        "{'@type':'am.yagson.Person','@val':{'name':'John','family':'Doe'}},'M']," +
                        "[{'@type':'am.yagson.Person','@val':{'name':'Jane','family':'Doe'}},'F']]}}"));
    }

    public void testMixedEmptyMap() {
        ClassWithMixedMap obj = objToTestWithEmptyMap();
        TestingUtils.testFully(obj, jsonStr(
                "{'map':{'@type':'java.util.HashMap','@val':{}}}"));
    }

    public void testMixedNumbersMap() {
        ClassWithMixedMap obj = objToTest();
        TestingUtils.testFully(obj, jsonStr(
                "{'map':{'@type':'java.util.HashMap','@val':[" +
                "[{'@type':'java.lang.Integer','@val':1},'int']," +
                "[{'@type':'java.lang.Long','@val':2},'long']," +
                "[{'@type':'java.math.BigDecimal','@val':1},{'@type':'java.lang.Integer','@val':1}]]}}"));
    }

    //
    // additional tests with local classes
    //

    private static class IntIntMap extends HashMap<Integer, Integer> {
    }

    public void testIntIntMap() {
        IntIntMap obj = new IntIntMap();
        obj.put(1, 2);
        TestingUtils.testFully(obj, jsonStr("{'1':2}"));
    }

    private static class WithIntIntMap {
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WithIntIntMap that = (WithIntIntMap) o;
            return !(map != null ? !map.equals(that.map) : that.map != null);

        }

        @Override
        public int hashCode() {
            return map != null ? map.hashCode() : 0;
        }
    }

    public void testWithIntIntMap() {
        WithIntIntMap obj = new WithIntIntMap();
        obj.map.put(1, 2);
        TestingUtils.testFully(obj, jsonStr(
                "{'map':{'1':2}}"));
    }


    private static class LongLongMap extends HashMap<Long, Long> {
    }

    public void testLongLongMap() {
        LongLongMap obj = new LongLongMap();
        obj.put(1L, 2L);
        TestingUtils.testFully(obj, jsonStr("{'1':2}"));
    }

    private static class BoolBoolMap extends HashMap<Boolean, Boolean> {
    }

    public void testBoolBoolLongMap() {
        BoolBoolMap obj = new BoolBoolMap();
        obj.put(false, true);
        TestingUtils.testFully(obj, jsonStr("{'false':true}"));
    }

    private static class PersonBoolMap extends HashMap<Person, Boolean> {
    }

    public void testPersonBoolLongMap() {
        PersonBoolMap obj = new PersonBoolMap();
        obj.put(new Person("John", "Doe"), true);
        TestingUtils.testFully(obj, jsonStr(
                "[[{'name':'John','family':'Doe'},true]]"));
    }


}
