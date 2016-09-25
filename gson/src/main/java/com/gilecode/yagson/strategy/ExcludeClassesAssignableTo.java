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

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Excludes classes which are assignable to one of the specified classes, i.e. these classes and all subclasses.
 *
 * @author Andrey Mogilev
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
