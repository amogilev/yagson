package am.yagson;

import java.math.BigDecimal;

import junit.framework.TestCase;

import static am.yagson.TestingUtils.jsonStr;

public class TestTypeInfoNumber extends TestCase {
	
	public void testInt() {
		/*
		FIXME: problem - reflective adapter is the only one which does not use wrappers for elements reading =>
		  it fails for simple adapters reading wrappers
		  Additionally, current simple vs complex scheme is not well-defined
		 V1: move wrapper-checks from wrappers (and Gson?) to ReadContext.doRead
		 V2: use wrapper in the reflective field wrapper (similar to others)
		 */
		TestingUtils.testFully(new ClassWithNumber(12), jsonStr(
				"{'num':{'@type':'java.lang.Integer','@val':12}}"));
	}
	
	public void testLong() {
		TestingUtils.testFully(new ClassWithNumber(12l), jsonStr(
				"{'num':12}"));
	}
	
	public void testDouble() {
		TestingUtils.testFully(new ClassWithNumber(12.01), jsonStr(
				"{'num':12.01}"));
	}
	
	public void testBigDecimal() {
		TestingUtils.testFully(new ClassWithNumber(BigDecimal.valueOf(12.01)), jsonStr(
				"{'num':{'@type':'java.math.BigDecimal','@val':12.01}}"));
	}
	
	

}
