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

public final class References {

    /**
     * The prefix which starts each reference path.
     * <p/>
     * When a reference string with such prefix is used as a string value, the
     * object at the corresponding reference path shall be applied instead.
     */
    public static final String REF_ROOT = "@root";

    /**
     * The prefix which starts the field references.
     * <p/>
     * When a field reference is used as a key in JSON object for a Map or Collection,
     * it designates the name and field for a value in the instance object to be applied,
     * rather than a general element of that Map or Collection.
     */
    public static final String REF_FIELD_PREFIX = "@.";

    /**
     * The reference used for hashcode fields with value equals to the actual hashcode.
     */
    public static final String REF_HASH = "@hash";

    // forbidden
    private References() {
    }

    private static final ReferencesPolicy defaultPolicy = ReferencesPolicy.DUPLICATE_OBJECTS;

    public static String keyRef(int i) {
        return "" + i + "-key";
    }

    public static String valRef(int i) {
        return "" + i + "-val";
    }

    public static ReferencesReadContext createReadContext(ReferencesPolicy policy) {
        if (policy == null) {
            policy = defaultPolicy;
        }

        return policy.getContextFactory().createReadContext();
    }

    public static ReferencesWriteContext createWriteContext(ReferencesPolicy policy, Object root) {
        if (policy == null) {
            policy = defaultPolicy;
        }

        return policy.getContextFactory().createWriteContext(root);
    }

    public static ReferencesPolicy defaultPolicy() {
        return defaultPolicy;
    }
}
