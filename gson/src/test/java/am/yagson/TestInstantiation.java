package am.yagson;

import junit.framework.TestCase;

public class TestInstantiation extends TestCase {
	
	public void testNoDefaultConstr() {
		TestingUtils.testFully(new NoDefaultConstrClass("foo", 11));
	}
	
	public void testMinedDefaultConstr() {
		TestingUtils.testFully(new MinedDefaultConstrClass("foo", 11));
	}
}
