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

import java.io.IOException;

import com.gilecode.yagson.WriteContext;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

public interface ReferencesWriteContext {
    /**
     * Writes the value using the specified type adapter, with the specified reference path element.
     * <p/>
     * If the same value is already emitted and is accessible from the current path, emits the reference to that
     * value instead. Otherwise, saves the just emitted value with the reference path constructed from the current
     * path and the specified new path element.
     */
    <T> void doWrite(T value, TypeAdapter<T> valueTypeAdapter, String pathElement, JsonWriter out,
                     WriteContext ctx) throws IOException;

    /**
     * Same as {@link #doWrite(Object, TypeAdapter, String, JsonWriter, WriteContext)}, but creates and returns
     * JSON tree elements instead of immediate writes to the writer.
     */
    <T> JsonElement doToJsonTree(T value, TypeAdapter<T> valueTypeAdapter, String pathElement, WriteContext ctx);

    /**
     * If the value to be emitted will be replaced with some reference, returns that reference. Otherwise, returns null.
     */
    <T> String getReferenceFor(T value, TypeAdapter<T> valueTypeAdapter, String pathElement);

    ReferencesPolicy getPolicy();
}
