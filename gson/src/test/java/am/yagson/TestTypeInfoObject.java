package am.yagson;

import java.math.BigDecimal;

import junit.framework.TestCase;

import static am.yagson.TestingUtils.jsonStr;

public class TestTypeInfoObject extends TestCase {
	
	public void testInt() {
		TestingUtils.testFully(new ClassWithObject(12), jsonStr(
				"{'@vtype':'java.lang.Integer','obj':12}"));
	}
	
	public void testDouble() {
		TestingUtils.testFully(new ClassWithObject(12.0), jsonStr(
				"{'@vtype':'java.lang.Double','obj':12.0}"));
	}
	
	public void testString() {
		TestingUtils.testFully(new ClassWithObject("foo"), jsonStr(
				"{'@vtype':'java.lang.String','obj':'foo'}"));
	}
	
	public void testPerson() {
		TestingUtils.testFully(new ClassWithObject(new Person("foo", "bar")), jsonStr(
				"{'@vtype':'am.yagson.Person','obj':{'name':'foo','family':'bar'}}"));
	}
	
	public void testBigDecimal() {
		TestingUtils.testFully(new ClassWithObject(BigDecimal.valueOf(12.01)), jsonStr(
				"{'@vtype':'java.math.BigDecimal','obj':12.01}"));
	}
}
