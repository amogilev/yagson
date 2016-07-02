package am.yagson;

import java.math.BigDecimal;

import junit.framework.TestCase;

import static am.yagson.TestingUtils.jsonStr;

public class TestTypeInfoMixedCollection extends TestCase {

	private static final String EXPECTED = jsonStr("{'l':[" +
			"{'@type':'java.lang.Integer','@val':1},2," +
			"{'@type':'java.lang.Float','@val':3.01},4.02," +
			"{'@type':'java.math.BigDecimal','@val':1}]}");

	private static final String JSON_ALT = jsonStr("{'l':{'@type':'java.util.ArrayList', '@val':[" +
			"{'@type':'java.lang.Integer','@val':1},2," +
			"{'@type':'java.lang.Float','@val':3.01},4.02," +
			"{'@type':'java.math.BigDecimal','@val':1}]}}\n");

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
		TestingUtils.testFullyByToString(obj, EXPECTED);
	}
	
	public void testMixedNumbersByEquals() {
		ClassWithMixedCollection obj = objToTest();
		TestingUtils.testFully(obj, EXPECTED);
	}

	public void testAlt1Deserialize() {
		ClassWithMixedCollection obj = objToTest();
		TestingUtils.testDeserialize(new YaGson(), JSON_ALT, obj, null);
	}

	public void testMixedPersonString() {
		ClassWithMixedCollection obj = new ClassWithMixedCollection();
		obj.add(new Person("foo", "bar"));
		
		TestingUtils.testFullyByToString(obj);
	}
}
