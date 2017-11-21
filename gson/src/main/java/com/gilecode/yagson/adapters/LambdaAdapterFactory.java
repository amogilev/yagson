/*
 * Copyright (C) 2017 Andrey Mogilev
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
package com.gilecode.yagson.adapters;

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;
import com.gilecode.yagson.reflection.ReflectionAccessUtils;
import com.gilecode.yagson.reflection.ReflectionAccessor;
import com.gilecode.yagson.types.NonSerializableLambdaException;
import com.gilecode.yagson.types.TypeUtils;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Provides type adapters for serializable and non-serializable lambdas.
 *
 * @author Andrey Mogilev
 */
public class LambdaAdapterFactory implements TypeAdapterFactory {

    private static final String SERIALIZED_LAMBDA_CLASS_NAME = "java.lang.invoke.SerializedLambda";
    private final Class<?> serializedLambdaClass;
    private final boolean enabled;
    private final ReflectionAccessor accessor = ReflectionAccessUtils.getReflectionAccessor();

    private class AdaptersHolder {
        final TypeAdapter serializedLambdaAdapter;
        final TypeAdapter serializableLambdaAdapter;
        final TypeAdapter nonSerializableLambdaAdapter;
        final TypeAdapter<Object> reflectiveSerializedLambdaAdapter;

        @SuppressWarnings("unchecked")
        AdaptersHolder(Gson gson) {
            serializedLambdaAdapter = new SerializedLambdaAdapter();
            serializableLambdaAdapter = new SerializableLambdaAdapter();
            nonSerializableLambdaAdapter = new NSLambdaAdapter();

            TypeToken tt = TypeToken.get(serializedLambdaClass);
            reflectiveSerializedLambdaAdapter = gson.getReflectiveTypeAdapterFactory().create(gson, tt);
        }
    }
    private volatile AdaptersHolder adaptersHolder;

    public LambdaAdapterFactory() {
        Class cl = null;
        try {
            cl = Class.forName(SERIALIZED_LAMBDA_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            // not in Java 8, no lambdas expected
        }
        serializedLambdaClass = cl;
        enabled = serializedLambdaClass != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (enabled) {
            Class<? super T> rawType = type.getRawType();
            if (TypeUtils.isLambdaClass(rawType) || rawType == serializedLambdaClass) {
                if (adaptersHolder == null) {
                    // lazy init of adapters
                    adaptersHolder = new AdaptersHolder(gson);
                }
                if (rawType == serializedLambdaClass) {
                    return adaptersHolder.serializedLambdaAdapter;
                } else if (Serializable.class.isAssignableFrom(rawType)) {
                    return adaptersHolder.serializableLambdaAdapter;
                } else {
                    return adaptersHolder.nonSerializableLambdaAdapter;
                }
            }
        }
        return null;
    }

    private class NSLambdaAdapter<T> extends SimpleTypeAdapter<T> {
        @Override
        public void write(JsonWriter out, T value) throws IOException {
            throw new IllegalStateException("Shouldn't be called");
        }

        @Override
        public void write(JsonWriter out, T value, WriteContext ctx) throws IOException {
            switch (ctx.getNsLambdaPolicy()) {
                case TO_NULL:
                    out.nullValue();
                    break;
                case ERROR:
                    throw new NonSerializableLambdaException();
            }
        }

        @Override
        public T read(JsonReader in) throws IOException {
            return null;
        }
    }

    private class SerializableLambdaAdapter<T> extends TypeAdapter<T> {

        @Override
        public void write(JsonWriter out, T value, WriteContext ctx) throws IOException {
            Object serializedLambda;
            try {
                Method writeReplaceMethod = value.getClass().getDeclaredMethod("writeReplace");
                accessor.makeAccessible(writeReplaceMethod);
                serializedLambda = writeReplaceMethod.invoke(value);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to obtain SerializedLambda using writeReplace()");
            }

            TypeUtils.writeTypeWrapperAndValue(out, serializedLambda, adaptersHolder.serializedLambdaAdapter, ctx);
        }

        @Override
        @SuppressWarnings("unchecked")
        public T read(JsonReader in, ReadContext ctx) throws IOException {
            // this happens only if de-serialization type is the actual lambda class
            // however, the type info shall be present so we can use reflective adapter, which will forward to
            // serializedLambdaAdapter
            return (T)adaptersHolder.reflectiveSerializedLambdaAdapter.read(in, ctx);
        }
    }

    private class SerializedLambdaAdapter extends TypeAdapter<Object> {
        private final Method readResolveMethod;

        @SuppressWarnings("unchecked")
        SerializedLambdaAdapter() {
            try {
                readResolveMethod = serializedLambdaClass.getDeclaredMethod("readResolve");
                accessor.makeAccessible(readResolveMethod);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Failed to obtain SerializedLambda::readResolve method");
            }
        }

        @Override
        public void write(JsonWriter out, Object serializedLambda, WriteContext ctx) throws IOException {
            adaptersHolder.reflectiveSerializedLambdaAdapter.write(out, serializedLambda, ctx);
        }

        @Override
        public Object read(JsonReader in, ReadContext ctx) throws IOException {
            Object serializedLambda = adaptersHolder.reflectiveSerializedLambdaAdapter.read(in, ctx);

            try {
                return readResolveMethod.invoke(serializedLambda);
            } catch (Exception e) {
                throw new JsonIOException("Failed to resolve lambda " + serializedLambda, e);
            }
        }
    }

}
