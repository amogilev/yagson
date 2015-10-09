package am.yagson;

import am.yagson.WithInnerClass.Inner;
import junit.framework.TestCase;

public class TestInnerClass extends TestCase {
	
	public void testClassWithInner() {
		WithInnerClass obj = new WithInnerClass();
		obj.outerStr = "bar";
		obj.makeInner("foo");
		
		WithInnerClass found = TestingUtils.testFully(obj);
		assertEquals("bar", found.inner.getOuterStr());
		
	}
	
	public void testInner() {
		WithInnerClass obj = new WithInnerClass();
		obj.outerStr = "bar";
		obj.makeInner("foo");
		
		Inner found = (Inner)TestingUtils.testJsonWithoutEquals(obj.inner);
		assertEquals("foo", found.innerStr);
		assertEquals("bar", found.getOuterStr());
	}
}
