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
import java.util.concurrent.atomic.AtomicReference;

import am.yagson.ReadContext;
import am.yagson.WriteContext;
import am.yagson.refs.PlaceholderUse;
import am.yagson.refs.ReferencePlaceholder;

import am.yagson.types.TypeUtils;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Types;
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
  private final Class<E[]> arrayType;
  private final TypeAdapter<E> componentTypeAdapter;

  public ArrayTypeAdapter(Gson context, TypeAdapter<E> componentTypeAdapter, Class<E> componentType) {
    this.componentTypeAdapter = new TypeAdapterRuntimeTypeWrapper<E>(context,
            componentTypeAdapter, componentType, TypeUtils.getEmitTypeInfoRule(context, false));
    this.componentType = componentType;
    arrayType = (Class<E[]>) Array.newInstance(componentType, 0).getClass();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Object readOptionallyAdvisedInstance(Object advisedInstance, JsonReader in,
                                                 ReadContext ctx) throws IOException {

    List<E> list = new ArrayList<E>();

    ReferencePlaceholder<E[]> arrayPlaceholder = new ReferencePlaceholder<E[]>();
    final AtomicReference<E[]> futureArray = new AtomicReference<E[]>();

    // PROBLEM: we should register the created array instance before reading the element,
    // but it is not possible as the array size is not known untul all elements are read.
    // If the temporary List is registered instead, self-referenced arrays are not de-serialized
    // correctly.
    //
    // SOLUTION: ReferencePlaceholder is registered instead of the array. At every place
    // where this reference is used, a PlaceholderUse is created and registered. When the
    // final array object is created, it is sent to all registered uses, which insert it
    // into the places of use.
    ctx.registerObject(arrayPlaceholder);

    Class advisedComponentType = null;
    boolean hasTypeAdvise = false;
    if (in.peek() == JsonToken.BEGIN_OBJECT) {
      Class typeAdvise = $Gson$Types.getRawType(TypeUtils.readTypeAdvice(in));
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
      ReferencePlaceholder<E> elementPlaceholder;

      E instance = ctx.doRead(in, componentTypeAdapter, Integer.toString(i));

      if (instance == null && ((elementPlaceholder = ctx.refsContext().consumeLastPlaceholderIfAny()) != null)) {
        final int fi = i;
        elementPlaceholder.registerUse(new PlaceholderUse<E>() {
          public void applyActualObject(E actualObject) {
            Array.set(futureArray.get(), fi, actualObject);
          }
        });
        // null will be added to the list now, and it will be replaced to an actual object in future
      }

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
    futureArray.set(arrayType.cast(array));
    arrayPlaceholder.applyActualObject(futureArray.get());

    return array;
  }

  @SuppressWarnings("unchecked")
  @Override public void write(JsonWriter out, Object array, WriteContext ctx) throws IOException {
    if (array == null) {
      out.nullValue();
      return;
    }

    out.beginArray();
    for (int i = 0, length = Array.getLength(array); i < length; i++) {
      E value = (E) Array.get(array, i);
      ctx.doWrite(value, componentTypeAdapter, Integer.toString(i), out);
    }
    out.endArray();
  }
}
