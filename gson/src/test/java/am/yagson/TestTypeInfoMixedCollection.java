package am.yagson;

import java.math.BigDecimal;

import junit.framework.TestCase;

public class TestTypeInfoMixedCollection extends TestCase {

	public static final String EXPECTED = "{\"@vtype\":\"java.util.ArrayList\",\"l\":[" +
			"{\"@type\":\"java.lang.Integer\",\"@val\":1},{\"@type\":\"java.lang.Long\",\"@val\":2}," +
			"{\"@type\":\"java.lang.Float\",\"@val\":3.01},{\"@type\":\"java.lang.Double\",\"@val\":4.02}," +
			"{\"@type\":\"java.math.BigDecimal\",\"@val\":1}]}";

	public static final String JSON_ALT = "{\"l\":{\"@type\":\"java.util.ArrayList\", \"@val\":[" +
			"{\"@type\":\"java.lang.Integer\",\"@val\":1},{\"@type\":\"java.lang.Long\",\"@val\":2}," +
			"{\"@type\":\"java.lang.Float\",\"@val\":3.01},{\"@type\":\"java.lang.Double\",\"@val\":4.02}," +
			"{\"@type\":\"java.math.BigDecimal\",\"@val\":1}]}}\n";

	private ClassWithMixedCollection objToTest() {
		ClassWithMixedCollection obj = new ClassWithMixedCollection();
		obj.add(1);
		obj.add(2L);
		obj.add(3.01f);
		obj.add(4.02);
		obj.add(BigDecimal.ONE);
		return obj;
	}

	public void testMixedNumbers() {
		ClassWithMixedCollection obj = objToTest();
		TestingUtils.testFullyByToString(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, EXPECTED);
	}
	
	public void testMixedNumbersByEquals() {
		ClassWithMixedCollection obj = objToTest();
		TestingUtils.testFully(obj, TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES, EXPECTED);
	}

	public void testAltDeserialize() {
		ClassWithMixedCollection obj = objToTest();
		TestingUtils.testDeserialize(JSON_ALT, obj);
	}

	
	public void testMixedPersonString() {
		ClassWithMixedCollection obj = new ClassWithMixedCollection();
		obj.add(new Person("foo", "bar"));
		
		TestingUtils.testFullyByToString(obj);
	}
}
