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

import java.io.IOException;

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Type adapter for primitive or simple values, which are known to not contain any child objects inside, so
 * the generated JSON representation does not contain JSON Objects or Arrays ('{...}' and '[...]'}.
 * <p/>
 * In particular, it means that the corresponding objects cannot contain circular dependencies.
 * <p/>
 * For convenience, in all current references policies, no references are created for such simple objects.
 * Otherwise, we could have many annoying references for values like empty string, zero etc.
 *
 * @author Andrey Mogilev
 */
public abstract class SimpleTypeAdapter<T> extends TypeAdapter<T> {

    abstract public void write(JsonWriter out, T value) throws IOException;
    abstract public T read(JsonReader in) throws IOException;

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public T read(JsonReader in, ReadContext ctx) throws IOException {
        T value = read(in);
        ctx.registerObject(value, true);
        return value;
    }

    @Override
    public void write(JsonWriter out, T value, WriteContext ctx) throws IOException {
        write(out, value);
    }
}
