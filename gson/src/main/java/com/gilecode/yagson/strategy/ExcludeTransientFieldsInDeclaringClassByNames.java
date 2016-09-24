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
