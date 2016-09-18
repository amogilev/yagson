package com.gilecode.yagson.strategy;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Excludes transient fields in the specified class only (no subclasses) by names.
 */
public class ExcludeTransientFieldsInDeclaringClassByNames implements TransientFieldExclusionStrategy {

    private final Class<?> declaringClass;
    private final Set<String> skipFieldNames;

    public ExcludeTransientFieldsInDeclaringClassByNames(Class<?> declaringClass, String...fieldNames) {
        this.declaringClass = declaringClass;
        this.skipFieldNames = new HashSet<String>(asList(fieldNames));
    }

    @Override
    public boolean shouldSkipField(Field f) {
        return declaringClass.equals(f.getDeclaringClass()) && skipFieldNames.contains(f.getName());
    }
}
