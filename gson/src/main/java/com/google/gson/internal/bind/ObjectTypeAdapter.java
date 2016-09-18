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

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;
import com.gilecode.yagson.refs.PlaceholderUse;
import com.gilecode.yagson.refs.ReferencePlaceholder;

import com.gilecode.yagson.types.TypeUtils;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapts types whose static type is only 'Object'. Uses getClass() on
 * serialization and a primitive/Map/List on deserialization.
 */
public final class ObjectTypeAdapter extends TypeAdapter<Object> {
  public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (type.getRawType() == Object.class) {
        return (TypeAdapter<T>) new ObjectTypeAdapter(gson);
      }
      return null;
    }
  };

  private final Gson gson;

  private ObjectTypeAdapter(Gson gson) {
    this.gson = gson;
  }

  @Override public Object read(JsonReader in, ReadContext ctx) throws IOException {
    JsonToken token = in.peek();
    switch (token) {
    case BEGIN_ARRAY:
      final List<Object> list = new ArrayList<Object>();
      ctx.registerObject(list, false);
      
      in.beginArray();
      for (int i = 0; in.hasNext(); i++) {
        Object elementInstance = ctx.doRead(in, this, Integer.toString(i));
        ReferencePlaceholder<Object> elementPlaceholder;
        if (elementInstance == null && ((elementPlaceholder = ctx.refsContext().consumeLastPlaceholderIfAny()) != null)) {
          final int fi = i;
          elementPlaceholder.registerUse(new PlaceholderUse<Object>() {
            public void applyActualObject(Object actualObject) {
              list.set(fi, actualObject);
            }
          });
          // null will be added to the list now, and it will be replaced to an actual object in future
        }

        list.add(elementInstance);
      }
      in.endArray();
      return list;

    case BEGIN_OBJECT: {
      Object result = null;

      in.beginObject();
      if (in.hasNext()) {
        LinkedTreeMap<String, Object> map = null;
        for (int i = 0; in.hasNext(); i++) {
          final String fieldName = in.nextName();
          if (i == 0) {
            if (fieldName.equals("@type")) {
              return TypeUtils.readTypeAdvisedValueAfterTypeField(gson, in, null, ctx);
            } else {
              result = map = new LinkedTreeMap<String, Object>();
              ctx.registerObject(result, false);
            }
          }
          ReferencePlaceholder<Object> valuePlaceholder;
          Object value = ctx.doRead(in, this, "" + i + "-val");
          if (value == null && ((valuePlaceholder = ctx.refsContext().consumeLastPlaceholderIfAny()) != null)) {
            final LinkedTreeMap<String, Object> fmap = map;
            valuePlaceholder.registerUse(new PlaceholderUse<Object>() {
              public void applyActualObject(Object actualObject) {
                fmap.put(fieldName, actualObject);
              }
            });
          } else {
            map.put(fieldName, value);
          }
        }
      } else {
        result = gson.getTypeInfoPolicy().isEnabled() ? new Object() : new LinkedTreeMap<String, Object>();
        ctx.registerObject(result, false);
      }
      in.endObject();
      return result;
    }
    case STRING:
      String str = in.nextString();
      if (ctx.refsContext().isReferenceString(str)) {
        return ctx.refsContext().getReferencedObject(str);
      } else {
        ctx.registerObject(str, false);
        return str;
      }

    case NUMBER:
      Number num = in.nextNumber();
      ctx.registerObject(num, false);
      return num;

    case BOOLEAN:
      boolean boolValue = in.nextBoolean();
      ctx.registerObject(boolValue, false);
      return boolValue;

    case NULL:
      in.nextNull();
      ctx.registerObject(null, false);
      return null;

    default:
      throw new IllegalStateException();
    }
  }

  @SuppressWarnings("unchecked")
  @Override public void write(JsonWriter out, Object value, WriteContext ctx) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }

    TypeAdapter<Object> typeAdapter = (TypeAdapter<Object>) gson.getAdapter(value.getClass());
    if (typeAdapter instanceof ObjectTypeAdapter) {
      out.beginObject();
      out.endObject();
      return;
    }

    typeAdapter.write(out, value, ctx);
  }
}
