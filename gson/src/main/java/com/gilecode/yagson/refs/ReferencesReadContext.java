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

import com.gilecode.yagson.ReadContext;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.gilecode.yagson.adapters.SimpleTypeAdapter;
import com.google.gson.stream.JsonReader;

/**
 * A context which provides correct reading of references and replacing them with previously read
 * actual objects.
 *
 * @author Andrey Mogilev
 */
public interface ReferencesReadContext {

    /**
     * Registers an object which was just read, as corresponding to the current path.
     *
     * @param value the object created by de-serializers, optionally without fields/contents yet. If the
     *              adapter that created the object is {@link SimpleTypeAdapter}, it may pass {@code null}
     *              as a note that the object cannot be referenced
     * @param fromSimpleTypeAdapter whether invoked from a simple type adapter
     */
    void registerObject(Object value, boolean fromSimpleTypeAdapter);

    <T> T doRead(JsonReader reader, TypeAdapter<T> typeAdapter, String pathElement, ReadContext ctx) throws IOException;

    /**
     * Returns whether the specified string is a reference string, known in this context.
     */
    boolean isReferenceString(String str);

    /**
     * Returns the object corresponding to the specified reference string. If the reference is known, but
     * no actual object exist yet, returns {@code null} and sets the placeholder, which MUST be consumed
     * using {@link #consumeLastPlaceholderIfAny()} before any further call.
     *
     * @throws JsonSyntaxException if no referenced object is known for the specified reference
     * @throws IOException if read failed
     *
     * @return the object for the reference; or {@code null} if there is a placeholder set for this reference
     */
    <T> T getReferencedObject(String reference) throws IOException;

    /**
     * Consumes and returns the placeholder set by the last call of {@link #getReferencedObject(String)}
     */
    <T> ReferencePlaceholder<T> consumeLastPlaceholderIfAny();

    /**
     * Returns the policy implemented by this context.
     */
    ReferencesPolicy getPolicy();
}
