package com.gilecode.yagson;

import java.math.BigDecimal;

import junit.framework.TestCase;

import static com.gilecode.yagson.TestingUtils.jsonStr;

public class TestTypeInfoNumber extends TestCase {
	
	public void testInt() {
		TestingUtils.testFully(new ClassWithNumber(12), jsonStr(
				"{'num':{'@type':'java.lang.Integer','@val':12}}"));
	}
	
	public void testLong() {
		TestingUtils.testFully(new ClassWithNumber(12L), jsonStr(
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
