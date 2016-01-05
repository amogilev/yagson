package am.yagson;

import am.yagson.WithInnerClass.Inner;
import junit.framework.TestCase;

import static am.yagson.TestingUtils.jsonStr;

public class TestInnerClass extends TestCase {
	
	public void testClassWithInner() {
		WithInnerClass obj = new WithInnerClass();
		obj.outerStr = "bar";
		obj.makeInner("foo");
		
		WithInnerClass found = TestingUtils.testFully(obj, jsonStr(
				"{'outerStr':'bar','inner':{'innerStr':'foo','this$0':'@root'}}"));
		assertEquals("bar", found.inner.getOuterStr());
		
	}
	
	public void testInner() {
		WithInnerClass obj = new WithInnerClass();
		obj.outerStr = "bar";
		obj.makeInner("foo");
		
		Inner found = (Inner)TestingUtils.testJsonWithoutEquals(obj.inner, jsonStr(
				"{'innerStr':'foo','this$0':{'outerStr':'bar','inner':'@root'}}"));

		assertEquals("foo", found.innerStr);
		assertEquals("bar", found.getOuterStr());
	}
}
