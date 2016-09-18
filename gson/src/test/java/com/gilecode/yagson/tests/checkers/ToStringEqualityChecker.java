package com.gilecode.yagson.tests.checkers;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Checker that verifies objects equality by the equality of 'toString()' resulting strings.
 */
public class ToStringEqualityChecker implements EqualityChecker {

    private static ToStringEqualityChecker instance = new ToStringEqualityChecker();

    public static ToStringEqualityChecker getInstance() {
        return instance;
    }

    private ToStringEqualityChecker() {
    }

    @Override
    public void assertEquality(Object o1, Object o2) {
        String str1, str2;
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            str1 = Arrays.deepToString((Object[])o1);
            str2 = Arrays.deepToString((Object[])o2);
        } else {
            str1 = o1.toString();
            str2 = o2.toString();
        }
        assertEquals("Deserialized object's toString() is not equal to the original",
                str1, str2);
    }

    @Override
    public String toString() {
        return "ToStringEqualityChecker";
    }
}
