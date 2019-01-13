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
package com.gilecode.yagson.adapters;

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Special low-level type adapters which does not perform any processing of types or references YaGson metadata, so
 * do not use contexts at all. It is supposed that low-level representation of that metadata is processed somewhere
 * else.
 * <p/>
 * For example, a type adapter for {@link com.google.gson.JsonElement} would treats references as simple JSON strings,
 * so it is a task of {@link com.google.gson.internal.bind.JsonTreeReader} to convert the references to actual objects.
 * Similarly, for writes, it is a task of some external class to convert type/references information into the
 * corresponding JSON elements.
 *
 * @author Andrey Mogilev
 */
public abstract class RawTypeAdapter<T> extends TypeAdapter<T> {

    abstract public void write(JsonWriter out, T value) throws IOException;
    abstract public T read(JsonReader in) throws IOException;

    @Override
    public T read(JsonReader in, ReadContext ctx) throws IOException {
        // ignore context
        return read(in);
    }

    @Override
    public void write(JsonWriter out, T value, WriteContext ctx) throws IOException {
        // ignore context
        write(out, value);
    }
}
