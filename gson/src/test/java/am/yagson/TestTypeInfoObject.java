package am.yagson;

import java.math.BigDecimal;

import am.yagson.types.TypeInfoPolicy;
import junit.framework.TestCase;

import static am.yagson.TestingUtils.jsonStr;
import static am.yagson.types.TypeInfoPolicy.EMIT_TYPE_WRAPPERS;
import static am.yagson.types.TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES;

public class TestTypeInfoObject extends TestCase {
	
	public void testInt() {
		TestingUtils.testFully(new ClassWithObject(12), EMIT_WRAPPERS_OR_VTYPES, jsonStr(
				"{'@vtype':'java.lang.Integer','obj':12}"));
		TestingUtils.testFully(new ClassWithObject(12), EMIT_TYPE_WRAPPERS, jsonStr(
				"{'obj':{'@type':'java.lang.Integer','@val':12}}"));
	}
	
	public void testDouble() {
		TestingUtils.testFully(new ClassWithObject(12.0), EMIT_WRAPPERS_OR_VTYPES, jsonStr(
				"{'@vtype':'java.lang.Double','obj':12.0}"));
		TestingUtils.testFully(new ClassWithObject(12.0), EMIT_TYPE_WRAPPERS, jsonStr(
				"{'obj':{'@type':'java.lang.Double','@val':12.0}}"));
	}
	
	public void testString() {
		TestingUtils.testFully(new ClassWithObject("foo"), EMIT_WRAPPERS_OR_VTYPES, jsonStr(
				"{'@vtype':'java.lang.String','obj':'foo'}"));
		TestingUtils.testFully(new ClassWithObject("foo"), EMIT_TYPE_WRAPPERS, jsonStr(
				"{'obj':{'@type':'java.lang.String','@val':'foo'}}"));
	}
	
	public void testBigDecimal() {
		TestingUtils.testFully(new ClassWithObject(BigDecimal.valueOf(12.01)), EMIT_WRAPPERS_OR_VTYPES, jsonStr(
				"{'@vtype':'java.math.BigDecimal','obj':12.01}"));
		TestingUtils.testFully(new ClassWithObject(BigDecimal.valueOf(12.01)), EMIT_TYPE_WRAPPERS, jsonStr(
				"{'obj':{'@type':'java.math.BigDecimal','@val':12.01}}"));
	}

	public void testPerson() {
		TestingUtils.testFully(new ClassWithObject(new Person("foo", "bar")), EMIT_WRAPPERS_OR_VTYPES, jsonStr(
				"{'@vtype':'am.yagson.Person','obj':{'name':'foo','family':'bar'}}"));
		TestingUtils.testFully(new ClassWithObject(new Person("foo", "bar")), EMIT_TYPE_WRAPPERS, jsonStr(
				"{'obj':{'@type':'am.yagson.Person','@val':{'name':'foo','family':'bar'}}}"));
	}

	public void testPersonEx() {
		TestingUtils.testFully(new ClassWithPerson(new PersonEx("foo", "bar", "addr")),
				EMIT_WRAPPERS_OR_VTYPES, jsonStr(
				"{'@vtype':'am.yagson.PersonEx','person':{'address':'addr','name':'foo','family':'bar'}}"));
		TestingUtils.testFully(new ClassWithPerson(new PersonEx("foo", "bar", "addr")),
				EMIT_TYPE_WRAPPERS, jsonStr(
				"{'person':{'@type':'am.yagson.PersonEx','@val':{'address':'addr','name':'foo','family':'bar'}}}"));
	}

}
