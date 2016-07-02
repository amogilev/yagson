package am.yagson.refs.impl;

import java.io.IOException;

import am.yagson.ReadContext;
import am.yagson.WriteContext;
import am.yagson.refs.ReferencePlaceholder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import am.yagson.refs.ReferencesPolicy;
import am.yagson.refs.ReferencesReadContext;
import am.yagson.refs.ReferencesWriteContext;

public class ReferencesNoneModeContext implements ReferencesReadContext, ReferencesWriteContext {

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
