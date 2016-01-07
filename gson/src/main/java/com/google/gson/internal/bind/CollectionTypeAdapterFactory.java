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

import am.yagson.refs.PlaceholderUse;
import am.yagson.refs.ReferencePlaceholder;
import am.yagson.refs.ReferencesReadContext;
import am.yagson.refs.ReferencesWriteContext;

import am.yagson.types.TypeUtils;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Adapt a homogeneous collection of objects.
 */
public final class CollectionTypeAdapterFactory implements TypeAdapterFactory {
  private final ConstructorConstructor constructorConstructor;

  public CollectionTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
  }

  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
    Type type = typeToken.getType();

    Class<? super T> rawType = typeToken.getRawType();
    if (!Collection.class.isAssignableFrom(rawType)) {
      return null;
    }

    Type elementType = $Gson$Types.getCollectionElementType(type, rawType);
    TypeAdapter<?> elementTypeAdapter = gson.getAdapter(TypeToken.get(elementType));
    ObjectConstructor<T> constructor = constructorConstructor.get(typeToken);

    @SuppressWarnings({"unchecked", "rawtypes"}) // create() doesn't define a type parameter
    TypeAdapter<T> result = new Adapter(gson, elementType, elementTypeAdapter, constructor, constructorConstructor);
    return result;
  }

  private static final class Adapter<E> extends TypeAdvisableComplexTypeAdapter<Collection<E>> {
    private final TypeAdapter<E> elementTypeAdapter;
    private final ObjectConstructor<? extends Collection<E>> constructor;
    private final ConstructorConstructor constructorConstructor;

    public Adapter(Gson context, Type elementType,
        TypeAdapter<E> elementTypeAdapter,
        ObjectConstructor<? extends Collection<E>> constructor,
        ConstructorConstructor constructorConstructor) {
      this.elementTypeAdapter =
          new TypeAdapterRuntimeTypeWrapper<E>(context, elementTypeAdapter, elementType, false);
      this.constructor = constructor;
      this.constructorConstructor = constructorConstructor;
    }


    @Override
    protected Collection<E> readOptionallyAdvisedInstance(Collection<E> advisedInstance, JsonReader in, ReferencesReadContext rctx)
        throws IOException {

      Collection<E> instance = null;
      boolean hasTypeAdvise = false;
      if (in.peek() == JsonToken.BEGIN_OBJECT) {
        Class typeAdvise = TypeUtils.readTypeAdvice(in);
        if (Collection.class.isAssignableFrom(typeAdvise)) {
          instance = (Collection<E>) constructorConstructor.get(TypeToken.get(typeAdvise)).construct();
        }
        TypeUtils.consumeValueField(in);
        hasTypeAdvise = true;
      }
      if (instance == null) {
        instance = advisedInstance == null ? constructor.construct() : advisedInstance;
      }
      rctx.registerObject(instance, false);
      final Collection<E> finstance = instance;

      if (in.peek() == JsonToken.BEGIN_ARRAY) {
        in.beginArray();
        for (int i = 0; in.hasNext(); i++) {
          E elementInstance = rctx.doRead(in, elementTypeAdapter, Integer.toString(i));
          ReferencePlaceholder<E> elementPlaceholder;
          if (elementInstance == null && ((elementPlaceholder = rctx.consumeLastPlaceholderIfAny()) != null)) {
            final int fi = i;
            elementPlaceholder.registerUse(new PlaceholderUse<E>() {
              public void applyActualObject(E actualObject) {
                if (finstance instanceof List) {
                  ((List<E>)finstance).set(fi, actualObject);
                } else  {
                  // no set() method available, have to re-build the collection to ensure the right order
                  List<E> l = new ArrayList<E>(finstance);
                  finstance.clear();
                  finstance.addAll(l.subList(0, fi));
                  finstance.add(actualObject);
                  finstance.addAll(l.subList(fi + 1, l.size()));
                }
              }
            });
            // null will be added to the list now, and it will be replaced to an actual object in future
          }

          instance.add(elementInstance);
        }
        in.endArray();
      }
      if (hasTypeAdvise) {
        in.endObject();
      }

      return instance;
    }

    public void write(JsonWriter out, Collection<E> collection, ReferencesWriteContext rctx) throws IOException {
      if (collection == null) {
        out.nullValue();
        return;
      }

      out.beginArray();
      int i = 0;
      for (E element : collection) {
        rctx.doWrite(element, elementTypeAdapter, Integer.toString(i), out);
        i++;
      }
      out.endArray();
    }
  }
}
