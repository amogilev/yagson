/*
 * Copyright (C) 2017 Andrey Mogilev
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
package com.gilecode.yagson.reflection;

import java.lang.reflect.AccessibleObject;

/**
 * Provides a replacement for {@link AccessibleObject#setAccessible(boolean)}, useful when that basic operation is
 * prohibited, e.g. throws {@link java.lang.reflect.InaccessibleObjectException} in Java 9.
 *
 * @author Andrey Mogilev
 */
public interface ReflectionAccessor {

    /**
     * Does the same as {@code ao.setAccessible(true)}, but never throws
     * {@link java.lang.reflect.InaccessibleObjectException}
     */
    void makeAccessible(AccessibleObject ao);

}
