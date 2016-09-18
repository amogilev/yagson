package com.gilecode.yagson.tests.checkers;

import static junit.framework.Assert.assertNull;

/**
 * Checker that verifies that the de-serialized object is null.
 */
public class NullChecker implements EqualityChecker {

    private static NullChecker instance = new NullChecker();

    public static NullChecker getInstance() {
        return instance;
    }

    private NullChecker() {
    }

    @Override
    public void assertEquality(Object o1, Object o2) {
        assertNull(o2);
    }

    @Override
    public String toString() {
        return "NullChecker";
    }
}
