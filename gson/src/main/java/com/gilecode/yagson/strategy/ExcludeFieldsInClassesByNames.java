/*
 * Copyright (C) 2016 Andrey Mogilev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gilecode.yagson.strategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Excludes fields in the specified classes or their subclasses by the field names.
 *
 * @author Andrey Mogilev
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
