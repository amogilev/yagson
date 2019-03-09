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
package com.gilecode.yagson.refs;

import com.gilecode.yagson.refs.impl.*;

/**
 * Enumerates supported references policies.
 *
 * @author Andrey Mogilev
 */
public enum ReferencesPolicy {

    /**
     * No references are allowed (same as in original Gson).
     * <p/>
     * Occurrences of circular references in serialized objects would lead to errors
     * (most probably {@link StackOverflowError}, but also could be {@link OutOfMemoryError}
     * with extensive usage of CPU and other resources before the error is thrown)
     */
    DISABLED(new ReferencesNoneModeContextFactory()),

    /**
     * Detects circular references in serialized objects and immediately throws {@link CircularReferenceException}.
     * <p/>
     * This policy provides ability to use YaGson in the "safe Gson" mode, i.e. be fully compatible with Gson but
     * prevent errors related to circular references.
     */
    DETECT_CIRCULAR_AND_THROW(new ReferencesThrowModeContextFactory()),

    /**
     * Supports circular references by serializing them as paths to the original object starting from the root
     * serialized object, e.g. "@root.anArrayField.0".
     * <p/>
     * This is the minimal mode required to avoid {@link StackOverflowError}. However, a lot of standard
     * collections may function improperly, as they contain multiple "views" to the same backing object,
     * e.g. see Collections$SynchronizedSortedSet.
     */
    CIRCULAR_ONLY(new ReferencesCircularOnlyModeContextFactory()),

    /**
     * Supports circular references as in {@link #CIRCULAR_ONLY} mode, but also detects duplicate sibling fields in
     * each object and serializes them as "sibling references". For example, if there are two equal fields "a" and "b"
     * in the same object, then a value of the field "b" is serialized as "@.a".
     * <p/>
     * Fixes some (but not all!) of the issues with multiple "views" in the standard collections. For example,
     * Collections$SynchronizedSortedSet is deserialized correctly in this mode, but
     * {@link java.util.concurrent.BlockingQueue}s} is not.
     */
    CIRCULAR_AND_SIBLINGS(new ReferencesCircularAndSiblingsContextFactory()),

    /**
     * 'Full' mode - all objects (except of Numbers and Strings) are checked for duplication and serialized as
     * either paths from the root object or sibling references.
     * <p/>
     * This mode is default one, as other modes may yield incorrect behavior of the de-serialized objects, such as
     * {@link java.util.concurrent.BlockingQueue}s}.
     */
    DUPLICATE_OBJECTS(new ReferencesAllDuplicatesModeContextFactory());

    private ReferencesContextFactory contextFactory;

    ReferencesPolicy(ReferencesContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    public ReferencesContextFactory getContextFactory() {
        return contextFactory;
    }

    public boolean isEnabled() { return this != DISABLED; }
}
