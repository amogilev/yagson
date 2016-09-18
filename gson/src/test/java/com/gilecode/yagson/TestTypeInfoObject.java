package com.gilecode.yagson;

import java.math.BigDecimal;

import junit.framework.TestCase;

import static com.gilecode.yagson.TestingUtils.jsonStr;

public class TestTypeInfoObject extends TestCase {
	
	public void testInt() {
		TestingUtils.testFully(new ClassWithObject(12), jsonStr(
				"{'obj':{'@type':'java.lang.Integer','@val':12}}"));
	}
	
	public void testDouble() {
		TestingUtils.testFully(new ClassWithObject(12.0), jsonStr(
				"{'obj':12.0}"));
	}
	
	public void testString() {
		TestingUtils.testFully(new ClassWithObject("foo"), jsonStr(
				"{'obj':'foo'}"));
	}
	
	public void testBigDecimal() {
		TestingUtils.testFully(new ClassWithObject(BigDecimal.valueOf(12.01)), jsonStr(
				"{'obj':{'@type':'java.math.BigDecimal','@val':12.01}}"));
	}

	public void testPerson() {
		TestingUtils.testFully(new ClassWithObject(new Person("foo", "bar")), jsonStr(
				"{'obj':{'@type':'com.gilecode.yagson.Person','@val':{'name':'foo','family':'bar'}}}"));
	}

	public void testPersonEx() {
		TestingUtils.testFully(new ClassWithPerson(new PersonEx("foo", "bar", "addr")), jsonStr(
				"{'person':{'@type':'com.gilecode.yagson.PersonEx','@val':{'address':'addr','name':'foo','family':'bar'}}}"));
	}

}
