package com.gilecode.yagson.tests.util;

/**
 * Collection of static utility methods useful for testing and validating the test results.
 *
 * @author Andrey Mogilev
 */
public class TestUtils {

    public static boolean hasMethod(Class<?> objClass, String name, Class<?>... parameterTypes) {
        try {
            objClass.getDeclaredMethod(name, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
