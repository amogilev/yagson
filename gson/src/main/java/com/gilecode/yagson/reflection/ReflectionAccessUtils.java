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

import com.gilecode.yagson.reflection.impl.PreJava9ReflectionAccessor;
import com.gilecode.yagson.reflection.impl.UnsafeReflectionAccessor;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

/**
 * Provides {@link ReflectionAccessor} instance which may be used to avoid reflective access issues
 * appeared in Java 9, like {@link java.lang.reflect.InaccessibleObjectException} thrown or
 * warnings like
 * <pre>
 *   WARNING: An illegal reflective access operation has occurred
 *   WARNING: Illegal reflective access by ...
 * </pre>
 * <p/>
 * Works both for Java 9 and earlier Java versions.
 *
 * @author Andrey Mogilev
 */
public class ReflectionAccessUtils {

    /**
     * Obtains a {@link ReflectionAccessor} instance suitable for the current Java version.
     * <p>
     * You may need one a reflective operation in your code throws {@link java.lang.reflect.InaccessibleObjectException}.
     * In such a case, use {@link ReflectionAccessor#makeAccessible(AccessibleObject)} on a field, method or constructor
     * (instead of basic {@link AccessibleObject#setAccessible(boolean)}).
     */
    public static ReflectionAccessor getReflectionAccessor() {
        return ReflectionAccessorHolder.instance;
    }

    // singleton holder
    private static class ReflectionAccessorHolder {
        static final ReflectionAccessor instance = createReflectionAccessor();
    }

    static int getMajorJavaVersion() {
        String[] parts = System.getProperty("java.version").split("[._]");
        int firstVer = Integer.parseInt(parts[0]);
        if (firstVer == 1 && parts.length > 1) {
            return Integer.parseInt(parts[1]);
        } else {
            return firstVer;
        }
    }

    static ReflectionAccessor createReflectionAccessor() {
        if (getMajorJavaVersion() < 9) {
            return new PreJava9ReflectionAccessor();
        } else {
            return new UnsafeReflectionAccessor();
        }
    }

}
