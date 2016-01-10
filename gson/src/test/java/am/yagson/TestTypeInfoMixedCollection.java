package am.yagson;

import java.math.BigDecimal;

import am.yagson.types.TypeInfoPolicy;
import junit.framework.TestCase;

import static am.yagson.TestingUtils.jsonStr;

public class TestTypeInfoMixedCollection extends TestCase {

	public static final String EXPECTED = jsonStr("{'l':[" +
			"{'@type':'java.lang.Integer','@val':1},2," +
			"{'@type':'java.lang.Float','@val':3.01},4.02," +
			"{'@type':'java.math.BigDecimal','@val':1}]}");

	public static final String JSON_ALT = jsonStr("{'l':{'@type':'java.util.ArrayList', '@val':[" +
			"{'@type':'java.lang.Integer','@val':1},2," +
			"{'@type':'java.lang.Float','@val':3.01},4.02," +
			"{'@type':'java.math.BigDecimal','@val':1}]}}\n");

	public static final String JSON_ALT2 = jsonStr("{'@vtype':'java.util.ArrayList','l':[" +
			"{'@type':'java.lang.Integer','@val':1},2," +
			"{'@type':'java.lang.Float','@val':3.01},4.02," +
			"{'@type':'java.math.BigDecimal','@val':1}]}");


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

	public void testAlt1Deserialize() {
		ClassWithMixedCollection obj = objToTest();
		TestingUtils.testDeserialize(new YaGson(), JSON_ALT, obj, null);
	}

	public void testAlt2Deserialize() {
		ClassWithMixedCollection obj = objToTest();
		TestingUtils.testDeserialize(new YaGson(), JSON_ALT2, obj, null);
	}

	public void testMixedPersonString() {
		ClassWithMixedCollection obj = new ClassWithMixedCollection();
		obj.add(new Person("foo", "bar"));
		
		TestingUtils.testFullyByToString(obj);
	}
}
