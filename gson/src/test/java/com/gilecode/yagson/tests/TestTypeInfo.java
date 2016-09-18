package com.gilecode.yagson.tests;

import java.math.BigDecimal;

import com.gilecode.yagson.tests.data.ClassWithObject;
import com.gilecode.yagson.tests.data.Person;
import com.gilecode.yagson.tests.data.PersonEx;
import com.gilecode.yagson.tests.util.BindingTestCase;

/**
 * Tests how type information is emitted for simple objects and numbers, and the cases where it is omitted.
 *
 * @author Andrey Mogilev
 */
public class TestTypeInfo extends BindingTestCase {
	
	public void testInt1() {
		test(new ClassWithNumber(12), jsonStr(
				"{'num':{'@type':'java.lang.Integer','@val':12}}"));
	}

    public void testInt2() {
        test(new ClassWithObject(12), jsonStr(
                "{'obj':{'@type':'java.lang.Integer','@val':12}}"));
    }
    
    public void testLong1() {
		test(new ClassWithNumber(12L), jsonStr(
				"{'num':12}"));
	}
	
    public void testLong2() {
		test(new ClassWithNumber(12L), jsonStr(
				"{'num':12}"));
	}
	
	public void testDouble1() {
		test(new ClassWithNumber(12.01), jsonStr(
				"{'num':12.01}"));
	}

    public void testDouble2() {
        test(new ClassWithObject(12.0), jsonStr(
                "{'obj':12.0}"));
    }

    public void testBigDecimal1() {
		test(new ClassWithNumber(BigDecimal.valueOf(12.01)), jsonStr(
				"{'num':{'@type':'java.math.BigDecimal','@val':12.01}}"));
	}

    public void testBigDecima2() {
        test(new ClassWithObject(BigDecimal.valueOf(12.01)), jsonStr(
                "{'obj':{'@type':'java.math.BigDecimal','@val':12.01}}"));
    }

    public void testString() {
        test(new ClassWithObject("foo"), jsonStr(
                "{'obj':'foo'}"));
    }

    public void testPerson1() {
        test(new ClassWithObject(new Person("foo", "bar")), jsonStr(
                "{'obj':{'@type':'com.gilecode.yagson.tests.data.Person','@val':{'name':'foo','family':'bar'}}}"));
    }

    public void testPerson2() {
        test(new ClassWithPerson(new Person("foo", "bar")), jsonStr(
                "{'person':{'name':'foo','family':'bar'}}"));
    }

    public void testPersonEx() {
        test(new ClassWithPerson(new PersonEx("foo", "bar", "addr")), jsonStr(
                "{'person':{'@type':'com.gilecode.yagson.tests.data.PersonEx','@val':{'address':'addr','name':'foo','family':'bar'}}}"));
    }

    private static class ClassWithPerson {
        Person person;

        ClassWithPerson(Person person) {
            this.person = person;
        }

        public Person getPerson() {
            return person;
        }
    }

    private static class ClassWithNumber {

        Number num;

        ClassWithNumber(Number num) {
            super();
            this.num = num;
        }
    }
}
