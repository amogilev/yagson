/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gson.internal.bind;

import am.yagson.ReadContext;
import am.yagson.WriteContext;
import am.yagson.types.TypeUtils;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Type adapter wrapper with the advanced support of the type advices and the default types. Emits/use type advices only
 * when the runtime type differs from the default type.
 * <p/>
 * This wrapper may only be used when the type info is enabled.
 */
public final class DefaultTypeAdapterRuntimeWrapper<T> extends TypeAdapter<T> {

  private final Gson gson;

  /**
   * The formal (super)type, corresponds to T
   */
  private final Type formalType;

  /**
   * The default class. Objects of this exact class are wriiten without type advices.
   */
  private final Class<? extends T> defaultRawType;

  /**
   * The type adapter used for objects of the default type.
   */
  private final TypeAdapter<T> defaultTypeAdapter;

  public DefaultTypeAdapterRuntimeWrapper(Gson gson, Class<? extends T> defaultRawType, Type formalType) {

    assert gson.getTypeInfoPolicy().isEnabled() : "Requires enabled type info";

    this.gson = gson;
    this.defaultRawType = defaultRawType;
    this.formalType = formalType;
    this.defaultTypeAdapter = getTypeAdapterFor(defaultRawType);
  }

  private TypeAdapter<T> getTypeAdapterFor(Class<? extends T> rawType) {
    Type type = TypeUtils.mergeTypes(rawType, formalType);
    return (TypeAdapter<T>) gson.getAdapter(TypeToken.get(type));
  }

  @Override
  public T read(JsonReader in, ReadContext ctx) throws IOException {

    // Although similar check exists in ReadContext.doRead(), we need to duplicate it here, as wrappers hide
    //   simple delegate adapters

    if (in.peek() == JsonToken.BEGIN_OBJECT && AdapterUtils.isSimpleTypeAdapter(defaultTypeAdapter)) {
      // if default adapter is simple and '{' is found, we expect and parse type advice here, and fail otherwise
      return TypeUtils.readTypeAdvisedValue(gson, in, formalType, ctx);

    } else {
      // no type advice, or defaultTypeAdapter is able to process type advice itself, as it is non-Simple
      return defaultTypeAdapter.read(in, ctx);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void write(JsonWriter out, T value, WriteContext ctx) throws IOException {
    boolean useDefault = value == null || value.getClass() == defaultRawType;
    TypeAdapter chosen = useDefault ? defaultTypeAdapter : getTypeAdapterFor((Class<? extends T>) value.getClass());
    if (useDefault) {
      chosen.write(out, value, ctx);
    } else {
      TypeUtils.writeTypeWrapperAndValue(out, value, chosen, ctx);
    }
  }
}
