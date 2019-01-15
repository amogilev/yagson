/*
 * Copyright (C) 2011 Google Inc.
 * Modifications copyright (C) 2016 Andrey Mogilev
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

package com.google.gson.internal.bind;

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;

import com.google.gson.*;
import com.google.gson.internal.$Gson$Preconditions;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Adapts a Gson 1.x tree-style adapter as a streaming TypeAdapter. Since the
 * tree adapter may be serialization-only or deserialization-only, this class
 * has a facility to lookup a delegate type adapter on demand.
 */
public final class TreeTypeAdapter<T> extends TypeAdapter<T> {
  private final JsonSerializer<T> serializer;
  private final JsonDeserializer<T> deserializer;
  final Gson gson;
  private final TypeToken<T> typeToken;
  private final TypeAdapterFactory skipPast;
  private final boolean simple;

  /** The delegate is lazily created because it may not be needed, and creating it may fail. */
  private TypeAdapter<T> delegate;

  public TreeTypeAdapter(JsonSerializer<T> serializer, JsonDeserializer<T> deserializer,
      Gson gson, TypeToken<T> typeToken, TypeAdapterFactory skipPast) {
    this.serializer = serializer;
    this.deserializer = deserializer;
    this.gson = gson;
    this.typeToken = typeToken;
    this.skipPast = skipPast;

    simple = checkIsSimpleConsistency(serializer, deserializer);
  }

  private boolean checkIsSimpleConsistency(JsonSerializer<T> serializer, JsonDeserializer<T> deserializer) throws IllegalArgumentException {
    if (serializer != null && deserializer != null) {
      if (serializer.isSimple() != deserializer.isSimple()) {
        throw new IllegalArgumentException("Serializer and deserializer must be consistent by isSimple()");
      }
      return serializer.isSimple();
    } else if (serializer != null) {
      return serializer.isSimple();
    } else if (deserializer != null) {
      return deserializer.isSimple();
    } else {
      return delegate().isSimple();
    }
//    } else {
//      TypeAdapter<T> delegate = delegate();
//      if (serializer != null && serializer.isSimple() != delegate.isSimple()) {
//        throw new IllegalArgumentException("Serializer and delegate type adapter must be consistent by isSimple()");
//      } else if (deserializer != null && deserializer.isSimple() != delegate.isSimple()) {
//        throw new IllegalArgumentException("Deserializer and delegate type adapter must be consistent by isSimple()");
//      }
//      return delegate.isSimple();
//    }
  }

  @Override
  public boolean isSimple() {
    return simple;
  }


  @Override public T read(JsonReader in, ReadContext ctx) throws IOException {
    if (deserializer == null) {
      return delegate().read(in, ctx);
    }
    JsonElement value = Streams.parse(in);
    if (value.isJsonNull()) {
      return null;
    } else if (value.isJsonPrimitive() && ((JsonPrimitive)value).isString()
            && ctx.refsContext().isReferenceString(value.getAsString())) {
      return ctx.refsContext().getReferencedObject(value.getAsString());
    }

    T obj = deserializer.deserialize(value, typeToken.getType(), asJsonContext(ctx));
    return obj;
  }

  @Override public void write(JsonWriter out, T value, WriteContext ctx) throws IOException {
    if (serializer == null) {
      delegate().write(out, value, ctx);
      return;
    }
    if (value == null) {
      out.nullValue();
      return;
    }
    JsonElement tree = serializer.serialize(value, typeToken.getType(), asJsonContext(ctx));
    Streams.write(tree, out);
  }

  private TypeAdapter<T> delegate() {
    TypeAdapter<T> d = delegate;
    return d != null
        ? d
        : (delegate = gson.getDelegateAdapter(skipPast, typeToken));
  }

  /**
   * Returns a new factory that will match each type against {@code exactType}.
   */
  public static TypeAdapterFactory newFactory(TypeToken<?> exactType, Object typeAdapter) {
    return new SingleTypeFactory(typeAdapter, exactType, false, null);
  }

  /**
   * Returns a new factory that will match each type and its raw type against
   * {@code exactType}.
   */
  public static TypeAdapterFactory newFactoryWithMatchRawType(
      TypeToken<?> exactType, Object typeAdapter) {
    // only bother matching raw types if exact type is a raw type
    boolean matchRawType = exactType.getType() == exactType.getRawType();
    return new SingleTypeFactory(typeAdapter, exactType, matchRawType, null);
  }

  /**
   * Returns a new factory that will match each type's raw type for assignability
   * to {@code hierarchyType}.
   */
  public static TypeAdapterFactory newTypeHierarchyFactory(
      Class<?> hierarchyType, Object typeAdapter) {
    return new SingleTypeFactory(typeAdapter, null, false, hierarchyType);
  }

  private static final class SingleTypeFactory implements TypeAdapterFactory {
    private final TypeToken<?> exactType;
    private final boolean matchRawType;
    private final Class<?> hierarchyType;
    private final JsonSerializer<?> serializer;
    private final JsonDeserializer<?> deserializer;

    SingleTypeFactory(Object typeAdapter, TypeToken<?> exactType, boolean matchRawType,
        Class<?> hierarchyType) {
      serializer = typeAdapter instanceof JsonSerializer
          ? (JsonSerializer<?>) typeAdapter
          : null;
      deserializer = typeAdapter instanceof JsonDeserializer
          ? (JsonDeserializer<?>) typeAdapter
          : null;
      $Gson$Preconditions.checkArgument(serializer != null || deserializer != null);
      this.exactType = exactType;
      this.matchRawType = matchRawType;
      this.hierarchyType = hierarchyType;
    }

    @SuppressWarnings("unchecked") // guarded by typeToken.equals() call
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      boolean matches = exactType != null
          ? exactType.equals(type) || matchRawType && exactType.getType() == type.getRawType()
          : hierarchyType.isAssignableFrom(type.getRawType());
      return matches
          ? new TreeTypeAdapter<T>((JsonSerializer<T>) serializer,
              (JsonDeserializer<T>) deserializer, gson, type, this)
          : null;
    }
  }

  private JsonDeserializationContext asJsonContext(final ReadContext readContext) {
    return new JsonDeserializationContext() {
      @SuppressWarnings("unchecked")
      @Override
      public <R> R deserialize(JsonElement json, String pathElement, Type typeOfT) throws JsonParseException {
        if (json == null || json.equals(JsonNull.INSTANCE)) {
          return null;
        }
        JsonReader jsonReader = new JsonTreeReader(json);
        TypeAdapter<Object> adapter = (TypeAdapter<Object>) gson.getAdapter(TypeToken.get(typeOfT));
        try {
          return (R) readContext.doRead(jsonReader, adapter, pathElement);
        } catch (IOException e) {
          throw new JsonIOException(e);
        }
      }

      @SuppressWarnings("unchecked")
      @Override
      public <R> R delegatedOrRootDeserialize(JsonElement json, Type typeOfT) throws JsonParseException {
        if (json == null || json.equals(JsonNull.INSTANCE)) {
          return null;
        }
        TypeAdapter<R> adapter = (TypeAdapter<R>) gson.getAdapter(TypeToken.get(typeOfT));
        return adapter.fromJsonTree(json, readContext);
      }

      @Override
      public ReadContext getReadContext() {
        return readContext;
      }
    };
  }


  private JsonSerializationContext asJsonContext(final WriteContext writeContext) {
    return new JsonSerializationContext() {
      @Override
      public JsonElement serialize(Object src, String pathElement) {
        return serialize(src, pathElement, Object.class);
      }

      @SuppressWarnings("unchecked")
      @Override
      public JsonElement serialize(Object src, String pathElement, Type deserializationType) {
        if (src == null) {
          return JsonNull.INSTANCE;
        }

        TypeAdapter<Object> adapter = (TypeAdapter<Object>) gson.getAdapter(TypeToken.get(deserializationType));
        return writeContext.doToJsonTree(src, adapter, pathElement);
      }

      @Override
      public JsonElement delegatedOrRootSerialize(Object src) {
        return delegatedOrRootSerialize(src, Object.class);
      }

      @SuppressWarnings("unchecked")
      @Override
      public JsonElement delegatedOrRootSerialize(Object src, Type deserializationType) {
        if (src == null) {
          return JsonNull.INSTANCE;
        }

        TypeAdapter<Object> adapter = (TypeAdapter<Object>) gson.getAdapter(TypeToken.get(deserializationType));
        return adapter.toJsonTree(src, writeContext);
      }

      @Override
      public WriteContext getWriteContext() {
        return writeContext;
      }
    };
  }

}
