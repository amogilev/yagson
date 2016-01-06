package am.yagson;

import am.yagson.types.TypeInfoPolicy;
import junit.framework.TestCase;

import java.math.BigDecimal;

import static am.yagson.TestingUtils.jsonStr;

public class TestTypeInfoMixedArray extends TestCase {

    private ClassWithMixedArray objToTestWithNumberArray() {
        return new ClassWithMixedArray<Number>(null, 1, 2L, 3.01f, 4.02, BigDecimal.ONE);
    }

    private ClassWithMixedArray objToTestWithObjectArray() {
        return new ClassWithMixedArray<Object>(null, 1, 2L, 3.01f, 4.02, BigDecimal.ONE);
    }

    public void testMixedNumbersInNumberArrayVtype() {
        ClassWithMixedArray obj = objToTestWithNumberArray();
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, jsonStr(
                "{'@vtype':'[Ljava.lang.Number;','arr':[null," +
                "{'@type':'java.lang.Integer','@val':1}," +
                "2," +
                "{'@type':'java.lang.Float','@val':3.01}," +
                "4.02," +
                "{'@type':'java.math.BigDecimal','@val':1}]}"));
    }

    public void testMixedNumbersInNumberArrayWrapper() {
        ClassWithMixedArray obj = objToTestWithNumberArray();
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_TYPE_WRAPPERS, jsonStr("{'arr':" +
                "{'@type':'[Ljava.lang.Number;'," +
                "'@val':[null," +
                "{'@type':'java.lang.Integer','@val':1}," +
                "2," +
                "{'@type':'java.lang.Float','@val':3.01}," +
                "4.02," +
                "{'@type':'java.math.BigDecimal','@val':1}]}}"));
    }

    public void testMixedNumbersInObjectArrayVtype() {
        ClassWithMixedArray obj = objToTestWithObjectArray();
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, jsonStr("{'arr':[null," +
                "{'@type':'java.lang.Integer','@val':1}," +
                "2," +
                "{'@type':'java.lang.Float','@val':3.01}," +
                "4.02," +
                "{'@type':'java.math.BigDecimal','@val':1}]}"));
    }

    public void testMixedNumbersInObjectArrayWrapper() {
        ClassWithMixedArray obj = objToTestWithObjectArray();
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_TYPE_WRAPPERS, jsonStr("{'arr':[null," +
                "{'@type':'java.lang.Integer','@val':1}," +
                "2," +
                "{'@type':'java.lang.Float','@val':3.01}," +
                "4.02," +
                "{'@type':'java.math.BigDecimal','@val':1}]}"));
    }

    public void testCustomInMixedObjectArray() {
        ClassWithMixedArray obj = new ClassWithMixedArray<Object>(new Person("foo", "bar"));

        TestingUtils.testFullyByToString(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, jsonStr(
                "{'arr':[" +
                "{'@type':'am.yagson.Person'," +
                "'@val':{'name':'foo','family':'bar'}}]}"));
    }

    public void testCustomInMixedCustomArrayVtype() {
        ClassWithMixedArray obj = new ClassWithMixedArray<Person>(new Person("foo", "bar"));

        TestingUtils.testFullyByToString(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES,
                jsonStr(
                        "{'@vtype':'[Lam.yagson.Person;'," +
                        "'arr':[{'@type':'am.yagson.Person','@val':{'name':'foo','family':'bar'}}]}"));
    }

    public void testCustomInMixedCustomArrayWrapper() {
        ClassWithMixedArray obj = new ClassWithMixedArray<Person>(new Person("foo", "bar"));

        TestingUtils.testFullyByToString(obj, TypeInfoPolicy.EMIT_TYPE_WRAPPERS,
                jsonStr("{'arr':" +
                        "{'@type':'[Lam.yagson.Person;'," +
                        "'@val':[{'@type':'am.yagson.Person','@val':{'name':'foo','family':'bar'}}]}}"));
    }

    public void testPureCustomArray() {
        ClassWithPersonArray obj = new ClassWithPersonArray(new Person("foo", "bar"));

        TestingUtils.testFullyByToString(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, jsonStr(
                "{'arr':[{'name':'foo','family':'bar'}]}"));
    }
}
