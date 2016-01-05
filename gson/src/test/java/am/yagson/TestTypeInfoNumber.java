package am.yagson;

import java.math.BigDecimal;

import junit.framework.TestCase;

import static am.yagson.TestingUtils.jsonStr;

public class TestTypeInfoNumber extends TestCase {
	
	public void testInt() {
		TestingUtils.testFully(new ClassWithNumber(12), jsonStr(
				"{'@vtype':'java.lang.Integer','num':12}"));
	}
	
	public void testLong() {
		TestingUtils.testFully(new ClassWithNumber(12l), jsonStr(
				"{'@vtype':'java.lang.Long','num':12}"));
	}
	
	public void testDouble() {
		TestingUtils.testFully(new ClassWithNumber(12.01), jsonStr(
				"{'@vtype':'java.lang.Double','num':12.01}"));
	}
	
	public void testBigDecimal() {
		TestingUtils.testFully(new ClassWithNumber(BigDecimal.valueOf(12.01)), jsonStr(
				"{'@vtype':'java.math.BigDecimal','num':12.01}"));
	}
	
	

}
