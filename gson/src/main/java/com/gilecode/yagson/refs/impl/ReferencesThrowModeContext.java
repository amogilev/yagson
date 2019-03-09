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

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.refs.*;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

/**
 * Provides dummy {@link ReferencesReadContext} and {@link ReferencesWriteContext} for the
 * {@link ReferencesPolicy#DISABLED} references policy, which do nothing.
 *
 * @author Andrey Mogilev
 */
class ReferencesThrowModeContext extends ReferencesCircularOnlyModeContext {

    private static ReferencesPolicy policy = ReferencesPolicy.DETECT_CIRCULAR_AND_THROW;

    static ReferencesReadContext readContextInstance = new ReferencesNoneModeContext.RefsReadContext() {
        @Override
        public ReferencesPolicy getPolicy() {
            return policy;
        }
    };

    class RefsWriteContext extends ReferencesCircularOnlyModeContext.RefsWriteContext {
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
        public ReferencesWriteContext makeChildContext() {
            return new RefsWriteContext(this);
        }

        @Override
        public <T> String getReferenceFor(T value, TypeAdapter<T> valueTypeAdapter, String pathElement) {
            String ref = super.getReferenceFor(value, valueTypeAdapter, pathElement);
            if (ref != null) {
                String curRef = getCurrentReference();
                throw new CircularReferenceException("Circular reference in a serialized object is detected: from " +
                        curRef + " to " + ref);
            }
            return null;
        }
    }



}
