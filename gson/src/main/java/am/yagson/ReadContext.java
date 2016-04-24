package am.yagson;

import am.yagson.refs.References;
import am.yagson.refs.ReferencesPolicy;
import am.yagson.refs.ReferencesReadContext;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

/**
 * The context used by type adapters for reading from JSON.
 */
public class ReadContext {

    /**
     * The context which contains references data.
     */
    private final ReferencesReadContext rctx;

    private ReadContext(ReferencesReadContext rctx) {
        this.rctx = rctx;
    }

    /**
     * Creates read context with the specified references policy.
     */
    public static ReadContext create(ReferencesPolicy policy) {
        return new ReadContext(References.createReadContext(policy));
    }

    public ReferencesReadContext refsContext() {
        return rctx;
    }

    public <T> T doRead(JsonReader reader, TypeAdapter<T> typeAdapter, String pathElement) throws IOException {
        return rctx.doRead(reader, typeAdapter, pathElement, this);
    }

    public <T> void registerObject(T instance) {
        rctx.registerObject(instance);
    }
}
