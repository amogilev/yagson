package com.gilecode.yagson.strategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Excludes classes which are assignable to one of the specified classes, i.e. these classes and all subclasses.
 */
public class ExcludeClassesAssignableTo implements ExclusionStrategy {

    private final List<Class<?>> skipTypes;

    public ExcludeClassesAssignableTo(Class<?>... skipTypes) {
        this.skipTypes = asList(skipTypes);
    }

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return false;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        for (Class<?> type : skipTypes) {
            if (type.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
}
