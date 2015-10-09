package am.yagson;

import java.math.BigDecimal;

import junit.framework.TestCase;

public class TestTypeInfoObject extends TestCase {
	
	public void testInt() {
		TestingUtils.testFully(new ClassWithObject(12));
	}
	
	public void testDouble() {
		TestingUtils.testFully(new ClassWithObject(12.0));
	}
	
	public void testString() {
		TestingUtils.testFully(new ClassWithObject("foo"));
	}
	
	public void testPerson() {
		TestingUtils.testFully(new ClassWithObject(new Person("foo", "bar")));
	}
	
	public void testBigDecimal() {
		TestingUtils.testFully(new ClassWithObject(BigDecimal.valueOf(12.01)));
	}
}
