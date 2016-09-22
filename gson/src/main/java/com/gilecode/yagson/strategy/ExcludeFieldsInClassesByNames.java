package com.gilecode.yagson.strategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

/**
 * Excludes fields in the specified class or its subclasses by names.
 */
public class ExcludeFieldsInClassesByNames implements ExclusionStrategy {

    private final Iterable<Class<?>> declaringSuperClasses;
    private final Set<String> skipFieldNames;

    public ExcludeFieldsInClassesByNames(Iterable<Class<?>> declaringSuperClasses, String...fieldNames) {
        this.declaringSuperClasses = declaringSuperClasses;
        this.skipFieldNames = new HashSet<String>(asList(fieldNames));
    }

    public ExcludeFieldsInClassesByNames(Class<?> declaringSuperClass, String...fieldNames) {
        this.declaringSuperClasses = Collections.<Class<?>>singleton(declaringSuperClass);
        this.skipFieldNames = new HashSet<String>(asList(fieldNames));
    }

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        if (skipFieldNames.contains(f.getName())) {
            for (Class<?> declaringSuperClass : declaringSuperClasses) {
                if (declaringSuperClass != null && declaringSuperClass.isAssignableFrom(f.getDeclaringClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
