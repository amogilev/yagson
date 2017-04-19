package com.gilecode.yagson.adapters;

import com.gilecode.yagson.types.TypeUtils;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Provides type adapters for serializable and non-serializable lambdas.
 */
public class LambdaAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (TypeUtils.isLambdaClass(type.getRawType())) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            TypeAdapter<T> result = (TypeAdapter)new NullLambdaAdapter();
            return result;
        }
        return null;
    }

    private class NullLambdaAdapter<T> extends SimpleTypeAdapter<T> {
        @Override
        public void write(JsonWriter out, T value) throws IOException {
            out.nullValue();
        }

        @Override
        public T read(JsonReader in) throws IOException {
            return null;
        }
    }
}
