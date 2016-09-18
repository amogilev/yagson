package com.gilecode.yagson.tests;

import com.gilecode.yagson.tests.util.BindingTestCase;

/**
 * Test for instantiation of classes with no default constructor and with a 'bad'
 * default constructor which throws exceptions.
 *
 * @author Andrey Mogilev
 */
public class TestInstantiation extends BindingTestCase {
	
	public void testNoDefaultConstr() {
		test(new NoDefaultConstrClass("foo", 11));
	}
	
	public void testMinedDefaultConstr() {
		test(new MinedDefaultConstrClass("foo", 11));
	}

	private static class NoDefaultConstrClass {
        String str;

        NoDefaultConstrClass(String str, int dummy) {
            this.str = str;
        }
    }

	private static class MinedDefaultConstrClass {

        final String str;

        public MinedDefaultConstrClass() {
            throw new Error("BOOOOM!!!");
        }

        MinedDefaultConstrClass(String str, int dummy) {
            this.str = str;
        }
    }
}
