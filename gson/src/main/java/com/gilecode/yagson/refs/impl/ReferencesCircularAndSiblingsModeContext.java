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

import com.gilecode.yagson.refs.References;
import com.gilecode.yagson.refs.ReferencesPolicy;
import com.gilecode.yagson.refs.ReferencesReadContext;
import com.gilecode.yagson.refs.ReferencesWriteContext;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;

import java.util.*;

/**
 * Provides {@link ReferencesReadContext} and {@link ReferencesWriteContext} for the
 * {@link ReferencesPolicy#CIRCULAR_AND_SIBLINGS} references policy.
 * <p/>
 * Used to find circular dependencies and duplicate field values during the
 * serialization, and write them as references.
 * <p/>
 * Neither this context, nor the Write or Read contexts are thread-safe!
 *
 * @author Andrey Mogilev
 */
class ReferencesCircularAndSiblingsModeContext extends ReferencesAllDuplicatesModeContext {
    private static ReferencesPolicy policy = ReferencesPolicy.CIRCULAR_AND_SIBLINGS;

    class RefsWriteContext extends ReferencesAllDuplicatesModeContext.RefsWriteContext {

        private Deque<Map<Object, String>> siblingReferencesStack;
        private Deque<Map<Object,String>> disposedRefsMapsCache;

        @Override
        protected void init() {
            super.init();
            siblingReferencesStack = new ArrayDeque<Map<Object, String>>();
            disposedRefsMapsCache = new ArrayDeque<Map<Object, String>>();
        }

        RefsWriteContext(Object root) {
            super(root);
        }

        @Override
        public ReferencesPolicy getPolicy() {
            return policy;
        }

        @Override
        public <T> String getReferenceFor(T value, TypeAdapter<T> valueTypeAdapter, String pathElement) {
            if (!isPotentialReference(value, valueTypeAdapter)) {
                return null;
            }

            boolean isField = pathElement.length() > 0 && Character.isJavaIdentifierStart(pathElement.charAt(0));
            if (isField) {
                // NOTE: only 'fields' are allowed, as this context require assistance of the reflective adapter to
                // resolve the sibling references during read (in the current implementation). No other adapters
                // currently support resolving FieldReferencePlaceholders
                Map<Object, String> siblingReferences = siblingReferencesStack.getLast();
                String ref = siblingReferences.get(value);
                if (ref != null) {
                    return References.REF_FIELD_PREFIX + ref;
                }
            }
            return super.getReferenceFor(value, valueTypeAdapter, pathElement);
        }

        @Override
        protected void startObject(Object value, String pathElement) {
            boolean isField = pathElement.length() > 0 && Character.isJavaIdentifierStart(pathElement.charAt(0));
            if (isField) {
                Map<Object, String> siblingReferences = siblingReferencesStack.getLast();
                siblingReferences.put(value, pathElement);
            }
            super.startObject(value, pathElement);
            siblingReferencesStack.add(getEmptyRefsMap());
       }

        @Override
        protected void endObject(Object value) {
            if (value != null) {
                disposeRefsMap(siblingReferencesStack.removeLast());
                super.endObject(value);
                references.remove(value);
            }
        }

        private void disposeRefsMap(Map<Object,String> map) {
            map.clear();
            disposedRefsMapsCache.add(map);
        }

        private Map<Object,String> getEmptyRefsMap() {
            if (disposedRefsMapsCache.isEmpty()) {
                return new IdentityHashMap<Object, String>();
            } else {
                return disposedRefsMapsCache.removeLast();
            }
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
            if (reference.startsWith(References.REF_FIELD_PREFIX)) {
                String fieldName = reference.substring(References.REF_FIELD_PREFIX.length());
                return new FieldReferencePlaceholder(fieldName);
            } else {
                Object value = objectsByReference.get(reference);
                if (value == null) {
                    if (!getCurrentReference().contains(reference)) {
                        throw new JsonSyntaxException("The reference cannot be read, as the current ReferencesPolicy " +
                                "allows only circular and sibling references: '" + reference + "'");
                    }
                    throw new JsonSyntaxException("Missing reference '" + reference + "'");
                }
                return value;
            }
        }
    }
}
