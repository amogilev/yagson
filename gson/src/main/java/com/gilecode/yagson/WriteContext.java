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
import com.gilecode.yagson.refs.ReferencesWriteContext;
import com.gilecode.yagson.types.NSLambdaPolicy;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * The context used by type adapters for writing to JSON.
 *
 * @author Andrey Mogilev
 */
public class WriteContext {

    /**
     * The Gson object which holds all configuration.
     */
    private final Gson gson;

    /**
     * The context which contains references data.
     */
    private final ReferencesWriteContext rctx;

    /**
     * Whether to skip writing of the entries of the next map,
     * i.e serialize it as empty map.
     */
    private boolean skipNextMapEntries;

    private NSLambdaPolicy nsLambdaPolicy;

    private WriteContext(Gson gson, ReferencesWriteContext rctx) {
        this.gson = gson;
        this.rctx = rctx;
        this.nsLambdaPolicy = gson.getNsLambdaPolicy();
    }

    public static WriteContext create(Gson gson, Object root) {
        return new WriteContext(gson, References.createWriteContext(gson.getReferencesPolicy(), root));
    }

    public ReferencesWriteContext refsContext() {
        return rctx;
    }

    public ReferencesPolicy refsPolicy() {
        return rctx.getPolicy();
    }

    public boolean isSkipNextMapEntries() {
        return skipNextMapEntries;
    }

    public NSLambdaPolicy getNsLambdaPolicy() {
        return nsLambdaPolicy;
    }

    public void setSkipNextMapEntries(boolean skipNextMapEntries) {
        this.skipNextMapEntries = skipNextMapEntries;
    }

    public void setNsLambdaPolicy(NSLambdaPolicy nsLambdaPolicy) {
        this.nsLambdaPolicy = nsLambdaPolicy;
    }

    public <T> JsonElement doToJsonTree(T value, TypeAdapter<T> valueTypeAdapter, String pathElement) {
        return rctx.doToJsonTree(value, valueTypeAdapter, pathElement, this);
    }

    public <T> void doWrite(T value, TypeAdapter<T> valueTypeAdapter, String pathElement, JsonWriter out) throws IOException {
        rctx.doWrite(value, valueTypeAdapter, pathElement, out, this);
    }

    public <T> String getReferenceFor(T value, TypeAdapter<T> valueTypeAdapter, String pathElement) {
        return rctx.getReferenceFor(value, valueTypeAdapter, pathElement);
    }

    public Gson getGson() {
        return gson;
    }

    public WriteContext makeChildContext() {
        WriteContext child = new WriteContext(gson, rctx.makeChildContext());
        child.skipNextMapEntries = skipNextMapEntries;
        child.nsLambdaPolicy = nsLambdaPolicy;
        return child;
    }

    public void mergeWithChildContext(WriteContext childContext) {
        rctx.mergeWithChildContext(childContext.rctx);
    }
}
