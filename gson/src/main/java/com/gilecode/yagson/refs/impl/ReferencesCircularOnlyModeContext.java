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
package com.gilecode.yagson.refs.impl;

import com.gilecode.yagson.refs.ReferencesPolicy;
import com.gilecode.yagson.refs.ReferencesReadContext;
import com.gilecode.yagson.refs.ReferencesWriteContext;
import com.google.gson.JsonSyntaxException;

/**
 * Provides {@link ReferencesReadContext} and {@link ReferencesWriteContext} for the
 * {@link ReferencesPolicy#CIRCULAR_ONLY} references policy.
 * <p/>
 * Used to find circular dependencies during the serialization, and write them as references.
 * <p/>
 * Neither this context, nor the Write or Read contexts are thread-safe!
 *
 * @author Andrey Mogilev
 */
class ReferencesCircularOnlyModeContext extends ReferencesAllDuplicatesModeContext {

    private static ReferencesPolicy policy = ReferencesPolicy.CIRCULAR_ONLY;

    class RefsWriteContext extends ReferencesAllDuplicatesModeContext.RefsWriteContext {
        RefsWriteContext(Object root) {
            super(root);
        }

        RefsWriteContext(RefsWriteContext parentContext) {
            super(parentContext);
        }

        @Override
        public ReferencesPolicy getPolicy() {
            return policy;
        }

        @Override
        protected void endObject(Object value) {
            super.endObject(value);
            if (value != null) {
                references.remove(value);
            }
        }

        @Override
        public ReferencesWriteContext makeChildContext() {
            return new RefsWriteContext(this);
        }
    }

    class RefsReadContext extends ReferencesAllDuplicatesModeContext.RefsReadContext {

        @Override
        public ReferencesPolicy getPolicy() {
            return policy;
        }

        @Override
        protected void afterObjectRead() {
            objectsByReference.remove(getCurrentReference());
            super.afterObjectRead();
        }

        @Override
        protected Object getObjectByReference(String reference) throws JsonSyntaxException {
            Object value = objectsByReference.get(reference);
            if (value == null) {
                if (!getCurrentReference().contains(reference)) {
                    throw new JsonSyntaxException("The reference cannot be read, as the current ReferencesPolicy " +
                            "allows only circular references: '" + reference + "'");
                }
                throw new JsonSyntaxException("Missing reference '" + reference + "'");
            }
            return value;
        }
    }
}
