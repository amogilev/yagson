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

import com.gilecode.yagson.refs.impl.ReferencesAllDuplicatesModeContextFactory;
import com.gilecode.yagson.refs.impl.ReferencesCircularAndSiblingsContextFactory;
import com.gilecode.yagson.refs.impl.ReferencesCircularOnlyModeContextFactory;
import com.gilecode.yagson.refs.impl.ReferencesNoneModeContextFactory;

/**
 * Enumerates supported references policies.
 *
 * @author Andrey Mogilev
 */
public enum ReferencesPolicy {

    /**
     * No references are allowed (same as in original Gson)
     */
    DISABLED(new ReferencesNoneModeContextFactory()),

    /**
     * Only the circular references are checked. This is the minimal mode required to avoid
     * {@link StackOverflowError}. However, a lot of standard collections may function improperly, as
     * they contain multiple "views" to the same backing object, e.g. see Collections$SynchronizedSortedSet.
     */
    CIRCULAR_ONLY(new ReferencesCircularOnlyModeContextFactory()),

    /**
     * Checks the circular references and duplicate fields in each object.
     * <p/>
     * Fixes some (but not all!) of the issues with multiple "views" in the standard collections. For example,
     * Collections$SynchronizedSortedSet is deserialized correctly in this mode, but
     * {@link java.util.concurrent.BlockingQueue}s} is not.
     */
    CIRCULAR_AND_SIBLINGS(new ReferencesCircularAndSiblingsContextFactory()),

    /**
     * 'Full' mode - all objects (except of Numbers and Strings) are checked for duplication.
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
