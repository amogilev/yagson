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
import am.yagson.types.EmitTypeInfoPredicate;
import am.yagson.types.TypeUtils;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public final class TypeAdapterRuntimeTypeWrapper<T> extends TypeAdapter<T> {
  private final Gson gson;
  private final TypeAdapter<T> delegate;
  private final Type type;
  private final EmitTypeInfoPredicate emitTypeInfoPredicate;

  public
  TypeAdapterRuntimeTypeWrapper(Gson gson, TypeAdapter<T> delegate, Type type,
                                EmitTypeInfoPredicate emitTypeInfoPredicate) {
    this.gson = gson;
    this.delegate = delegate;
    this.type = type;
    this.emitTypeInfoPredicate = emitTypeInfoPredicate;
  }

  @Override
  public T read(JsonReader in, ReadContext ctx) throws IOException {

    // Although similar check exists in ReadContext.doRead(), we need to duplicate it here, as wrappers hide
    //   simple delegate adapters

    if (in.peek() == JsonToken.BEGIN_OBJECT && AdapterUtils.isSimpleTypeAdapter(delegate)) {
      // if the delegate adapter is simple and '{' is found, we expect and parse type advice here, and fail otherwise
      return TypeUtils.readTypeAdvisedValue(gson, in, type, ctx);

    } else {
      // no type advice, or delegate is able to process type advice itself
      return delegate.read(in, ctx);
    }

  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void write(JsonWriter out, T value, WriteContext ctx) throws IOException {
    TypeAdapter chosen = chooseTypeAdapter(value);
    // TODO: move context.getTypeInfoPolicy().isEnabled() to predicates
    if (value != null && gson.getTypeInfoPolicy().isEnabled() &&
            emitTypeInfoPredicate.apply(value.getClass(), type)) {
      TypeUtils.writeTypeWrapperAndValue(out, value, chosen, ctx);
    } else {
      chosen.write(out, value, ctx);
    }
  }

  public TypeAdapter<T> resolve(T value) {
    TypeAdapter chosen = chooseTypeAdapter(value);
    boolean emitTypeInfo =  value != null && gson.getTypeInfoPolicy().isEnabled() &&
            emitTypeInfoPredicate.apply(value.getClass(), type);
    if (emitTypeInfo) {
      return new TypeInfoEmittingTypeAdapterWrapper<T>(chosen);
    } else {
      return chosen;
    }
  }
  
  @SuppressWarnings("rawtypes")
  private TypeAdapter chooseTypeAdapter(T value) {
    // Order of preference for choosing type adapters
    // First preference: a type adapter registered for the runtime type
    // Second preference: a type adapter registered for the declared type
    // Third preference: reflective type adapter for the runtime type (if it is a sub class of the declared type)
    // Fourth preference: reflective type adapter for the declared type
    TypeAdapter chosen = delegate;
    Type runtimeType = getRuntimeTypeIfMoreSpecific(type, value);
    if (runtimeType != type) {
      TypeAdapter runtimeTypeAdapter = gson.getAdapter(TypeToken.get(runtimeType));
      if (!(runtimeTypeAdapter instanceof ReflectiveTypeAdapterFactory.Adapter)) {
        // The user registered a type adapter for the runtime type, so we will use that
        chosen = runtimeTypeAdapter;
      } else if (!(delegate instanceof ReflectiveTypeAdapterFactory.Adapter) && !gson.getTypeInfoPolicy().isEnabled()) {
        // The user registered a type adapter for Base class, so we prefer it over the
        // reflective type adapter for the runtime type.
        // NOTE: disabled when emitTypeInfo is used
        chosen = delegate;
      } else {
        // Use the type adapter for runtime type
        chosen = runtimeTypeAdapter;
      }
    }
    return chosen;
  }
  
  /**
   * Finds a compatible runtime type if it is more specific
   */
  private Type getRuntimeTypeIfMoreSpecific(Type type, Object value) {
    if (value != null
        && (type == Object.class || type instanceof TypeVariable<?> || type instanceof Class<?>)) {
      type = value.getClass();
    }
    return type;
  }
}
