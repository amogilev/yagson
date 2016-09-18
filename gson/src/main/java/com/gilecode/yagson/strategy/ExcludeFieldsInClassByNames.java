package com.gilecode.yagson.strategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Excludes fields in the specified class or its subclasses by names.
 */
public class ExcludeFieldsInClassByNames implements ExclusionStrategy {

    private final Class<?> declaringSuperClass;
    private final Set<String> skipFieldNames;

    public ExcludeFieldsInClassByNames(Class<?> declaringSuperClass, String...fieldNames) {
        this.declaringSuperClass = declaringSuperClass;
        this.skipFieldNames = new HashSet<String>(asList(fieldNames));
    }

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return declaringSuperClass.isAssignableFrom(f.getDeclaringClass()) && skipFieldNames.contains(f.getName());
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
