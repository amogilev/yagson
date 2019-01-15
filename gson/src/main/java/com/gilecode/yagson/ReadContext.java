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
package com.gilecode.yagson;

import com.gilecode.yagson.refs.References;
import com.gilecode.yagson.refs.ReferencesPolicy;
import com.gilecode.yagson.refs.ReferencesReadContext;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.gilecode.yagson.adapters.TypeAdviceReadingSimpleAdapterWrapper;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;

/**
 * The context used by type adapters for reading from JSON.
 *
 * @author Andrey Mogilev
 */
public class ReadContext {

    /**
     * The context which contains references data.
     */
    private final ReferencesReadContext rctx;

    private final Gson gson;

    private ReadContext(ReferencesReadContext rctx, Gson gson) {
        this.rctx = rctx;
        this.gson = gson;
    }

    /**
     * Creates read context with the specified references policy.
     */
    public static ReadContext create(Gson gson) {
        return new ReadContext(References.createReadContext(gson.getReferencesPolicy()), gson);
    }

    public static ReadContext nullContext() {
        return new ReadContext(References.createReadContext(ReferencesPolicy.DISABLED), null);
    }

    public ReferencesReadContext refsContext() {
        return rctx;
    }

    public <T> T doRead(JsonReader reader, TypeAdapter<T> typeAdapter, String pathElement) throws IOException {

        // PROBLEM: as JsonReader does not support lookaheads for more than one token, we cannot
        //    distinguish regular objects like "{field:value}" from type-advised primitives like
        //    "{@type:Long, @val:1}" here.
        //
        // SOLUTION: all non-Simple TypeAdapters shall handle type advices themselves, so we delegate to them.
        //     For simple delegates, if '{' found, we expect and parse type advice here, and fail otherwise


        if (typeAdapter.isSimple() && reader.peek() == JsonToken.BEGIN_OBJECT &&
                gson.getTypeInfoPolicy().isEnabled()) {
            // simple type adapters do not use '{', so this is definitely a type advice. Use a wrapper adapter
            // to correctly process it
            typeAdapter = new TypeAdviceReadingSimpleAdapterWrapper<T>(gson, typeAdapter);
        }

        return rctx.doRead(reader, typeAdapter, pathElement, this);
    }

    public <T> void registerObject(T instance, boolean fromSimpleTypeAdapter) {
        rctx.registerObject(instance, fromSimpleTypeAdapter);
    }
}
