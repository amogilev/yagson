package com.gilecode.yagson.tests.util;

/**
 * Enumerates different kinds of equality-like checks, used to verify the correctness of the binding.
 *
 * @author Andrey Mogilev
 */
public enum EqualityCheckMode {

    /**
     * Check by equality of {@link Object#toString()}}.
     */
    TO_STRING,

    /**
     * Use {@link Object#equals(Object)}}}, with an additional special processing of
     * arrays and collections to provide some protection against infinite recursion and
     * ordering issues in unsorted sets and maps.
     */
    EQUALS,

    /**
     * Use reflective equality checks for all non-transient non-static object fields.
     */
    REFLECTIVE,

    /**
     * No checks are performed.
     */
    NONE,

    /**
     * Check that the result is null.
     */
    EXPECT_NULL,

    /**
     * Automatically applies one or more of other checks ({@link #TO_STRING}, {@link #EQUALS} or {@link #REFLECTIVE},
     * based on what methods are available on the compared classes.
     * <p/>
     * This is the default mode, which shall work good in most cases.
     */
    AUTO
}
