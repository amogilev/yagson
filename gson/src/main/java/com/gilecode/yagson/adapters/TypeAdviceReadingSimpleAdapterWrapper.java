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
import com.gilecode.yagson.adapters.AdapterUtils;
import com.gilecode.yagson.types.TypeUtils;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Type adapter wrapper with the support of the type advices at the reading stage. Used to provide type advices support
 * for simple type adapters.
 * <p/>
 * This wrapper may only be used when the type info is enabled.
 */
public final class TypeAdviceReadingSimpleAdapterWrapper<T> extends TypeAdapter<T> {

  private final Gson gson;

  /**
   * The type adapter used for objects of the default type.
   */
  private final TypeAdapter<T> delegateTypeAdapter;

  public TypeAdviceReadingSimpleAdapterWrapper(Gson gson, TypeAdapter<T> delegateSimpleTypeAdapter) {
    this.gson = gson;
    this.delegateTypeAdapter = delegateSimpleTypeAdapter;

    assert gson.getTypeInfoPolicy().isEnabled() : "Requires enabled type info";
    assert AdapterUtils.isSimpleTypeAdapter(delegateSimpleTypeAdapter) : "Expects delegate type adapter to be simple";
  }

  @Override
  public T read(JsonReader in, ReadContext ctx) throws IOException {

    if (in.peek() == JsonToken.BEGIN_OBJECT) {
      // if '{' is found for the simple type, we expect and parse type advice here, and fail otherwise
      return TypeUtils.readTypeAdvisedValue(gson, in, null, ctx);

    } else {
      // no type advice, use delegate
      return delegateTypeAdapter.read(in, ctx);
    }
  }

  @Override
  public void write(JsonWriter out, T value, WriteContext ctx) throws IOException {
    throw new IllegalStateException("This TypeAdapter is read-only");
  }
}
