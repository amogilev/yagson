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

import java.io.IOException;

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;
import com.gilecode.yagson.refs.ReferencePlaceholder;
import com.gilecode.yagson.refs.ReferencesPolicy;
import com.gilecode.yagson.refs.ReferencesReadContext;
import com.gilecode.yagson.refs.ReferencesWriteContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Provides dummy {@link ReferencesReadContext} and {@link ReferencesWriteContext} for the
 * {@link ReferencesPolicy#DISABLED} references policy, which do nothing.
 *
 * @author Andrey Mogilev
 */
class ReferencesNoneModeContext implements ReferencesReadContext, ReferencesWriteContext {

    public static ReferencesNoneModeContext instance = new ReferencesNoneModeContext();

    public <T> JsonElement doToJsonTree(T value, TypeAdapter<T> valueTypeAdapter,
                                        String pathElement, WriteContext ctx) {
        return valueTypeAdapter.toJsonTree(value, ctx);
    }

    public <T> String getReferenceFor(T value, TypeAdapter<T> valueTypeAdapter, String pathElement) {
        return null;
    }

    public <T> void doWrite(T value, TypeAdapter<T> valueTypeAdapter,
                            String pathElement, JsonWriter out, WriteContext ctx) throws IOException {
        valueTypeAdapter.write(out, value, ctx);
    }

    public void registerObject(Object value, boolean fromSimpleTypeAdapter) {
        // do nothing
    }

    public <T> T doRead(JsonReader reader, TypeAdapter<T> typeAdapter,
                        String pathElement, ReadContext ctx) throws IOException {
        return typeAdapter.read(reader, ctx);
    }

    public boolean isReferenceString(String str) {
        // no references are supported
        return false;
    }

    public <T> T getReferencedObject(String reference) throws IOException {
        throw new JsonSyntaxException("The reference cannot be read, as the current policy is ReferencesPolicy." +
                ReferencesPolicy.DISABLED + ": '" + reference + "'");
    }

    public <T> ReferencePlaceholder<T> consumeLastPlaceholderIfAny() {
        return null;
    }

    public ReferencesPolicy getPolicy() {
        return ReferencesPolicy.DISABLED;
    }
}
