package am.yagson;

import junit.framework.TestCase;

import java.math.BigDecimal;

public class TestTypeInfoMixedArray extends TestCase {

    public static final String EXPECTED_OBJ_ARRAY = "{\"arr\":[null," +
            "{\"@type\":\"java.lang.Integer\",\"@val\":1}," +
            "{\"@type\":\"java.lang.Long\",\"@val\":2}," +
            "{\"@type\":\"java.lang.Float\",\"@val\":3.01}," +
            "{\"@type\":\"java.lang.Double\",\"@val\":4.02}," +
            "{\"@type\":\"java.math.BigDecimal\",\"@val\":1}]}";

    public static final String EXPECTED_NUM_ARRAY_VTYPE = "{\"@vtype\":\"[Ljava.lang.Number;\"," +
            "\"arr\":[null," +
            "{\"@type\":\"java.lang.Integer\",\"@val\":1}," +
            "{\"@type\":\"java.lang.Long\",\"@val\":2}," +
            "{\"@type\":\"java.lang.Float\",\"@val\":3.01}," +
            "{\"@type\":\"java.lang.Double\",\"@val\":4.02}," +
            "{\"@type\":\"java.math.BigDecimal\",\"@val\":1}]}";

    public static final String EXPECTED_NUM_ARRAY_WRAPPER = "{\"arr\":" +
            "{\"@type\":\"[Ljava.lang.Number;\"," +
            "\"@val\":[null," +
            "{\"@type\":\"java.lang.Integer\",\"@val\":1}," +
            "{\"@type\":\"java.lang.Long\",\"@val\":2}," +
            "{\"@type\":\"java.lang.Float\",\"@val\":3.01}," +
            "{\"@type\":\"java.lang.Double\",\"@val\":4.02}," +
            "{\"@type\":\"java.math.BigDecimal\",\"@val\":1}]}}";

    public static final String EXPECTED_OBJ_ARRAY_PERSON = "{\"arr\":[" +
            "{\"@type\":\"am.yagson.Person\"," +
            "\"@val\":{\"name\":\"foo\",\"family\":\"bar\"}}]}";

    public static final String EXPECTED_PERSON_ARRAY_PERSON_WRAPPER = "{\"arr\":" +
            "{\"@type\":\"[Lam.yagson.Person;\"," +
            "\"@val\":[{\"@type\":\"am.yagson.Person\",\"@val\":{\"name\":\"foo\",\"family\":\"bar\"}}]}}";

    public static final String EXPECTED_PERSON_ARRAY_PERSON_VTYPE = "{\"@vtype\":\"[Lam.yagson.Person;\"," +
            "\"arr\":[{\"@type\":\"am.yagson.Person\",\"@val\":{\"name\":\"foo\",\"family\":\"bar\"}}]}";

    public static final String EXPECTED_PURE_PERSON_ARRAY = "{\"arr\":[{\"name\":\"foo\",\"family\":\"bar\"}]}";

    private ClassWithMixedArray objToTestWithNumberArray() {
        return new ClassWithMixedArray<Number>(null, 1, 2L, 3.01f, 4.02, BigDecimal.ONE);
    }

    private ClassWithMixedArray objToTestWithObjectArray() {
        return new ClassWithMixedArray<Object>(null, 1, 2L, 3.01f, 4.02, BigDecimal.ONE);
    }

    public void testMixedNumbersInNumberArrayVtype() {
        ClassWithMixedArray obj = objToTestWithNumberArray();
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, EXPECTED_NUM_ARRAY_VTYPE);
    }

    public void testMixedNumbersInNumberArrayWrapper() {
        ClassWithMixedArray obj = objToTestWithNumberArray();
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_TYPE_WRAPPERS, EXPECTED_NUM_ARRAY_WRAPPER);
    }

    public void testMixedNumbersInObjectArrayVtype() {
        ClassWithMixedArray obj = objToTestWithObjectArray();
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, EXPECTED_OBJ_ARRAY);
    }

    public void testMixedNumbersInObjectArrayWrapper() {
        ClassWithMixedArray obj = objToTestWithObjectArray();
        TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_TYPE_WRAPPERS, EXPECTED_OBJ_ARRAY);
    }

    public void testCustomInMixedObjectArray() {
        ClassWithMixedArray obj = new ClassWithMixedArray<Object>(new Person("foo", "bar"));

        TestingUtils.testFullyByToString(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, EXPECTED_OBJ_ARRAY_PERSON);
    }

    public void testCustomInMixedCustomArrayVtype() {
        ClassWithMixedArray obj = new ClassWithMixedArray<Person>(new Person("foo", "bar"));

        TestingUtils.testFullyByToString(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES,
                EXPECTED_PERSON_ARRAY_PERSON_VTYPE);
    }

    public void testCustomInMixedCustomArrayWrapper() {
        ClassWithMixedArray obj = new ClassWithMixedArray<Person>(new Person("foo", "bar"));

        TestingUtils.testFullyByToString(obj, TypeInfoPolicy.EMIT_TYPE_WRAPPERS,
                EXPECTED_PERSON_ARRAY_PERSON_WRAPPER);
    }

    public void testPureCustomArray() {
        ClassWithPersonArray obj = new ClassWithPersonArray(new Person("foo", "bar"));

        TestingUtils.testFullyByToString(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, EXPECTED_PURE_PERSON_ARRAY);
    }
}
