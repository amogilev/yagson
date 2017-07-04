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

import com.gilecode.yagson.types.TypeUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Provides the type adapter for {@link Class} serialization
 *
 * @author Andrey Mogilev
 */
public class ClassTypeAdapter extends SimpleTypeAdapter<Class> {

    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            return typeToken.getRawType() == Class.class ? (TypeAdapter<T>) new ClassTypeAdapter(gson) : null;
        }
    };

    private final Gson gson;

    private ClassTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, Class value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.getName());
        }
    }

    @Override
    public Class read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            String str = in.nextString();
            try {
                return TypeUtils.classForName(gson, str);
            } catch (ClassNotFoundException e) {
                throw new JsonSyntaxException("Missing class", e);
            }
        }
    }
}
