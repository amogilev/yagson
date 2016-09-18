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
