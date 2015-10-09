package am.yagson;

import java.math.BigDecimal;

import junit.framework.TestCase;

public class TestTypeInfoNumber extends TestCase {
	
	public void testInt() {
		TestingUtils.testFully(new ClassWithNumber(12));
	}
	
	public void testLong() {
		TestingUtils.testFully(new ClassWithNumber(12l));
	}
	
	public void testDouble() {
		TestingUtils.testFully(new ClassWithNumber(12.01));
	}
	
	public void testBigDecimal() {
		TestingUtils.testFully(new ClassWithNumber(BigDecimal.valueOf(12.01)));
	}
	
	

}
