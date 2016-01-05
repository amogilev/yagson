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

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import am.yagson.refs.ReferencesReadContext;
import am.yagson.refs.ReferencesWriteContext;

import am.yagson.types.TypeUtils;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Adapt an array of objects.
 */
public final class ArrayTypeAdapter<E> extends TypeAdvisableComplexTypeAdapter<Object> {
  public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
      Type type = typeToken.getType();
      if (!(type instanceof GenericArrayType || type instanceof Class && ((Class<?>) type).isArray())) {
        return null;
      }

      Type componentType = $Gson$Types.getArrayComponentType(type);
      TypeAdapter<?> componentTypeAdapter = gson.getAdapter(TypeToken.get(componentType));
      return new ArrayTypeAdapter(
              gson, componentTypeAdapter, $Gson$Types.getRawType(componentType));
    }
  };

  private final Class<E> componentType;
  private final TypeAdapter<E> componentTypeAdapter;
  private final ConstructorConstructor constructorConstructor;

  public ArrayTypeAdapter(Gson context, TypeAdapter<E> componentTypeAdapter, Class<E> componentType) {
    this.componentTypeAdapter =
      new TypeAdapterRuntimeTypeWrapper<E>(context, componentTypeAdapter, componentType);
    this.componentType = componentType;
    this.constructorConstructor = context.getConstructorConstructor();
  }

  @Override
  protected Object readOptionallyAdvisedInstance(Object advisedInstance, JsonReader in, ReferencesReadContext rctx) throws IOException {

    List<E> list = new ArrayList<E>();
    // TODO(amogilev): should register the array object instead, but it is not possible as we do not know it's
    //          size at this point! Probably need to use some placeholders with deferred Inserters...
    rctx.registerObject(list, false);

    Class advisedComponentType = null;
    boolean hasTypeAdvise = false;
    if (in.peek() == JsonToken.BEGIN_OBJECT) {
      Class typeAdvise = TypeUtils.readTypeAdvice(in);
      if (typeAdvise.isArray()) {
        advisedComponentType = typeAdvise.getComponentType();
      }
      TypeUtils.consumeValueField(in);
      hasTypeAdvise = true;
    } else if (advisedInstance != null && advisedInstance.getClass().isArray()) {
      advisedComponentType = advisedInstance.getClass().getComponentType();
    }

    in.beginArray();
    for (int i = 0; in.hasNext(); i++) {
      E instance = rctx.doRead(in, componentTypeAdapter, Integer.toString(i));
      list.add(instance);
    }
    in.endArray();

    if (hasTypeAdvise) {
      in.endObject();
    }

    Object array = Array.newInstance(advisedComponentType == null ? componentType : advisedComponentType, list.size());
    for (int i = 0; i < list.size(); i++) {
      Array.set(array, i, list.get(i));
    }
    return array;
  }

  @SuppressWarnings("unchecked")
  @Override public void write(JsonWriter out, Object array, ReferencesWriteContext rctx) throws IOException {
    if (array == null) {
      out.nullValue();
      return;
    }

    out.beginArray();
    for (int i = 0, length = Array.getLength(array); i < length; i++) {
      E value = (E) Array.get(array, i);
      rctx.doWrite(value, componentTypeAdapter, Integer.toString(i), out);
    }
    out.endArray();
  }
}
