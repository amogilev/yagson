package am.yagson;

import java.math.BigDecimal;

import junit.framework.TestCase;

public class TestTypeInfoMixedCollection extends TestCase {
	
	public void testMixedNumbers() {
		ClassWithMixedCollection obj = new ClassWithMixedCollection();
		
		obj.add(1);
		obj.add(2L);
		obj.add(3.01f);
		obj.add(4.02);
		obj.add(BigDecimal.ONE);
		
		TestingUtils.testFullyByToString(obj);
	}
	
	public void testMixedNumbersByEquals() {
		ClassWithMixedCollection obj = new ClassWithMixedCollection();
		
		obj.add(1);
		obj.add(2L);
		obj.add(3.01f);
		obj.add(4.02);
		obj.add(BigDecimal.ONE);
		
		TestingUtils.testFully(obj);
	}
	
	public void testMixedPersonString() {
		ClassWithMixedCollection obj = new ClassWithMixedCollection();
		obj.add(new Person("foo", "bar"));
		
		TestingUtils.testFullyByToString(obj);
	}
}
