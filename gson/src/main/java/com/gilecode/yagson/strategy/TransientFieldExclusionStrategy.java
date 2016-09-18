package com.gilecode.yagson.strategy;

import java.lang.reflect.Field;

/**
 * An alternative to {@link com.google.gson.ExclusionStrategy} which is applied only for transient fields allowed by the
 * general exclusion strategies.
 */
public interface TransientFieldExclusionStrategy {

    /**
     * @param f the field object that is under test
     * @return true if the field should be ignored; otherwise false
     */
    boolean shouldSkipField(Field f);
}
