package com.gilecode.yagson;

import com.gilecode.yagson.refs.References;
import com.gilecode.yagson.refs.ReferencesPolicy;
import com.gilecode.yagson.refs.ReferencesWriteContext;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * The context used by type adapters for writing to JSON.
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

    private WriteContext(Gson gson, ReferencesWriteContext rctx) {
        this.gson = gson;
        this.rctx = rctx;
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

    public void setSkipNextMapEntries(boolean skipNextMapEntries) {
        this.skipNextMapEntries = skipNextMapEntries;
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
}
