package am.yagson;

import am.yagson.types.TypeInfoPolicy;
import junit.framework.TestCase;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static am.yagson.TestingUtils.jsonStr;

public class TestTypeInfoMixedMap extends TestCase {

    private static final String EXPECTED_COMPLEX_VTYPE = jsonStr("{'@vtype':'java.util.HashMap','map':[" +
            "[{'@type':'am.yagson.Person','@val':{'name':'John','family':'Doe'}},{'@type':'java.lang.String','@val':'M'}]," +
            "[{'@type':'am.yagson.Person','@val':{'name':'Jane','family':'Doe'}},{'@type':'java.lang.String','@val':'F'}]]}");

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
        TestingUtils.testFully(objToTestComplexKey(), TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, EXPECTED_COMPLEX_VTYPE);
    }

    public void testMixedEmptyMapVtype() {
        ClassWithMixedMap obj = objToTestWithEmptyMap();
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, jsonStr(
                "{'@vtype':'java.util.HashMap','map':{}}"));
    }
    public void testMixedEmptyMapWrapper() {
        ClassWithMixedMap obj = objToTestWithEmptyMap();
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_TYPE_WRAPPERS, jsonStr(
                "{'map':{'@type':'java.util.HashMap','@val':{}}}"));
    }

    public void testMixedNumbersMapVtype() {
        ClassWithMixedMap obj = objToTest();
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, jsonStr(
                "{'@vtype':'java.util.HashMap','map':[" +
                "[{'@type':'java.lang.Integer','@val':1},{'@type':'java.lang.String','@val':'int'}]," +
                "[{'@type':'java.lang.Long','@val':2},{'@type':'java.lang.String','@val':'long'}]," +
                "[{'@type':'java.math.BigDecimal','@val':1},{'@type':'java.lang.Integer','@val':1}]]}"));
    }

    public void testMixedNumbersMapWrapper() {
        ClassWithMixedMap obj = objToTest();
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_TYPE_WRAPPERS, jsonStr(
                "{'map':{'@type':'java.util.HashMap','@val':[" +
                "[{'@type':'java.lang.Integer','@val':1},{'@type':'java.lang.String','@val':'int'}]," +
                "[{'@type':'java.lang.Long','@val':2},{'@type':'java.lang.String','@val':'long'}]," +
                "[{'@type':'java.math.BigDecimal','@val':1},{'@type':'java.lang.Integer','@val':1}]]}}"));
    }

    //
    // additional tests with local classes
    //

    public static class IntIntMap extends HashMap<Integer, Integer> {
    }

    public void testIntIntMap() {
        IntIntMap obj = new IntIntMap();
        obj.put(1, 2);
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, jsonStr("{'1':2}"));
    }

    public static class WithIntIntMap {
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
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, jsonStr(
                "{'map':{'1':2}}"));
    }


    public static class LongLongMap extends HashMap<Long, Long> {
    }

    public void testLongLongMap() {
        LongLongMap obj = new LongLongMap();
        obj.put(1L, 2L);
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, jsonStr("{'1':2}"));
    }

    public static class BoolBoolMap extends HashMap<Boolean, Boolean> {
    }

    public void testBoolBoolLongMap() {
        BoolBoolMap obj = new BoolBoolMap();
        obj.put(false, true);
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, jsonStr("{'false':true}"));
    }

    public static class PersonBoolMap extends HashMap<Person, Boolean> {
    }

    public void testPersonBoolLongMap() {
        PersonBoolMap obj = new PersonBoolMap();
        obj.put(new Person("John", "Doe"), true);
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, jsonStr(
                "[[{'name':'John','family':'Doe'},true]]"));
    }


}
