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

import static am.yagson.refs.References.keyRef;
import static am.yagson.refs.References.valRef;
import am.yagson.refs.ReferencesReadContext;
import am.yagson.refs.ReferencesWriteContext;

import am.yagson.types.TypeUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapts maps to either JSON objects or JSON arrays.
 *
 * <h3>Maps as JSON objects</h3>
 * For primitive keys or when complex map key serialization is not enabled, this
 * converts Java {@link Map Maps} to JSON Objects. This requires that map keys
 * can be serialized as strings; this is insufficient for some key types. For
 * example, consider a map whose keys are points on a grid. The default JSON
 * form encodes reasonably: <pre>   {@code
 *   Map<Point, String> original = new LinkedHashMap<Point, String>();
 *   original.put(new Point(5, 6), "a");
 *   original.put(new Point(8, 8), "b");
 *   System.out.println(gson.toJson(original, type));
 * }</pre>
 * The above code prints this JSON object:<pre>   {@code
 *   {
 *     "(5,6)": "a",
 *     "(8,8)": "b"
 *   }
 * }</pre>
 * But GSON is unable to deserialize this value because the JSON string name is
 * just the {@link Object#toString() toString()} of the map key. Attempting to
 * convert the above JSON to an object fails with a parse exception:
 * <pre>com.google.gson.JsonParseException: Expecting object found: "(5,6)"
 *   at com.google.gson.JsonObjectDeserializationVisitor.visitFieldUsingCustomHandler
 *   at com.google.gson.ObjectNavigator.navigateClassFields
 *   ...</pre>
 *
 * <h3>Maps as JSON arrays</h3>
 * An alternative approach taken by this type adapter when it is required and
 * complex map key serialization is enabled is to encode maps as arrays of map
 * entries. Each map entry is a two element array containing a key and a value.
 * This approach is more flexible because any type can be used as the map's key;
 * not just strings. But it's also less portable because the receiver of such
 * JSON must be aware of the map entry convention.
 *
 * <p>Register this adapter when you are creating your GSON instance.
 * <pre>   {@code
 *   Gson gson = new GsonBuilder()
 *     .registerTypeAdapter(Map.class, new MapAsArrayTypeAdapter())
 *     .create();
 * }</pre>
 * This will change the structure of the JSON emitted by the code above. Now we
 * get an array. In this case the arrays elements are map entries:
 * <pre>   {@code
 *   [
 *     [
 *       {
 *         "x": 5,
 *         "y": 6
 *       },
 *       "a",
 *     ],
 *     [
 *       {
 *         "x": 8,
 *         "y": 8
 *       },
 *       "b"
 *     ]
 *   ]
 * }</pre>
 * This format will serialize and deserialize just fine as long as this adapter
 * is registered.
 */
public final class MapTypeAdapterFactory implements TypeAdapterFactory {
  private final ConstructorConstructor constructorConstructor;
  private final boolean complexMapKeySerialization;

  public MapTypeAdapterFactory(ConstructorConstructor constructorConstructor,
      boolean complexMapKeySerialization) {
    this.constructorConstructor = constructorConstructor;
    this.complexMapKeySerialization = complexMapKeySerialization;
  }

  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
    Type type = typeToken.getType();

    Class<? super T> rawType = typeToken.getRawType();
    if (!Map.class.isAssignableFrom(rawType)) {
      return null;
    }

    Class<?> rawTypeOfSrc = $Gson$Types.getRawType(type);
    Type[] keyAndValueTypes = $Gson$Types.getMapKeyAndValueTypes(type, rawTypeOfSrc);
    TypeAdapter<?> keyAdapter = getKeyAdapter(gson, keyAndValueTypes[0]);
    TypeAdapter<?> valueAdapter = gson.getAdapter(TypeToken.get(keyAndValueTypes[1]));
    ObjectConstructor<T> constructor = constructorConstructor.get(typeToken);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // we don't define a type parameter for the key or value types
    TypeAdapter<T> result = new Adapter(gson, keyAndValueTypes[0], keyAdapter,
        keyAndValueTypes[1], valueAdapter, constructor);
    return result;
  }

  /**
   * Returns a type adapter that writes the value as a string.
   */
  private TypeAdapter<?> getKeyAdapter(Gson context, Type keyType) {
    return (keyType == boolean.class || keyType == Boolean.class)
        ? TypeAdapters.BOOLEAN_AS_STRING
        : context.getAdapter(TypeToken.get(keyType));
  }

  private final class Adapter<K, V> extends TypeAdvisableComplexTypeAdapter<Map<K, V>> {
    private final TypeAdapter<K> keyTypeAdapter;
    private final TypeAdapter<V> valueTypeAdapter;
    private final ObjectConstructor<? extends Map<K, V>> constructor;
    private final Gson gson;

    public Adapter(Gson gson, Type keyType, TypeAdapter<K> keyTypeAdapter,
                   Type valueType, TypeAdapter<V> valueTypeAdapter,
                   ObjectConstructor<? extends Map<K, V>> constructor) {
      this.keyTypeAdapter =
        new TypeAdapterRuntimeTypeWrapper<K>(gson, keyTypeAdapter, keyType, false);
      this.valueTypeAdapter =
        new TypeAdapterRuntimeTypeWrapper<V>(gson, valueTypeAdapter, valueType, false);
      this.constructor = constructor;
      this.gson = gson;
    }
    
    @Override
    protected Map<K, V> readOptionallyAdvisedInstance(Map<K, V> advisedInstance, JsonReader in,
                                                 ReferencesReadContext rctx) throws IOException {

      Map<K, V> instance = null;

      JsonToken peek = in.peek();

      if (peek == JsonToken.BEGIN_ARRAY) {
        instance = advisedInstance == null ? constructor.construct() : advisedInstance;
        rctx.registerObject(advisedInstance, false);

        in.beginArray();
        for (int i = 0; in.hasNext(); i++) {
          in.beginArray(); // entry array
          K key = rctx.doRead(in, keyTypeAdapter, keyRef(i));
          V value = rctx.doRead(in, valueTypeAdapter, valRef(i));
          V replaced = instance.put(key, value);
          if (replaced != null) {
            throw new JsonSyntaxException("duplicate key: " + key);
          }
          in.endArray();
        }
        in.endArray();
      } else {
        in.beginObject();

        boolean skipFirstPromotion = false;

        if (in.peek() == JsonToken.NAME) {
          // if type is advised with @type, follow that advice
          String keyStr = in.nextName();
          if (keyStr.equals("@type")) {
            return TypeUtils.readTypeAdvicedValueAfterTypeField(gson, in, rctx);
          } else {
            skipFirstPromotion = true;
            JsonReaderInternalAccess.INSTANCE.returnStringToBuffer(in, keyStr);
          }
        }

        // otherwise use the previous @vtype advice, or the default instance
        instance = advisedInstance == null ? constructor.construct() : advisedInstance;
        rctx.registerObject(advisedInstance, false);

        for (int i = 0; in.hasNext(); i++) {
          if (skipFirstPromotion) {
            skipFirstPromotion = false;
          } else {
            JsonReaderInternalAccess.INSTANCE.promoteNameToValue(in);
          }

          K key = rctx.doRead(in, keyTypeAdapter, keyRef(i));
          V value = rctx.doRead(in, valueTypeAdapter, valRef(i));
          V replaced = instance.put(key, value);
          if (replaced != null) {
            throw new JsonSyntaxException("duplicate key: " + key);
          }
        }
        in.endObject();
      }
      return instance;
    }

    public void write(JsonWriter out, Map<K, V> map, ReferencesWriteContext rctx) throws IOException {
      if (map == null) {
        out.nullValue();
        return;
      }

      if (!complexMapKeySerialization) {
        out.beginObject();
        int i = 0;
        for (Map.Entry<K, V> entry : map.entrySet()) {
          out.name(String.valueOf(entry.getKey()));
          rctx.doWrite(entry.getValue(), valueTypeAdapter, "" + i + "-val", out);
          i++;
        }
        out.endObject();
        return;
      }

      boolean hasComplexKeys = false;
      List<JsonElement> keys = new ArrayList<JsonElement>(map.size());

      List<V> values = new ArrayList<V>(map.size());
      int i = 0;
      for (Map.Entry<K, V> entry : map.entrySet()) {
        K key = entry.getKey();
        JsonElement keyElement = rctx.doToJsonTree(key, keyTypeAdapter, keyRef(i));
        i++;
        keys.add(keyElement);
        values.add(entry.getValue());
        hasComplexKeys |= keyElement.isJsonArray() || keyElement.isJsonObject();
      }

      if (hasComplexKeys) {
        out.beginArray();
        for (i = 0; i < keys.size(); i++) {
          out.beginArray(); // entry array
          Streams.write(keys.get(i), out);
          rctx.doWrite(values.get(i), valueTypeAdapter, valRef(i), out);
          out.endArray();
        }
        out.endArray();
      } else {
        out.beginObject();
        for (i = 0; i < keys.size(); i++) {
          JsonElement keyElement = keys.get(i);
          out.name(keyToString(keyElement));
          rctx.doWrite(values.get(i), valueTypeAdapter, valRef(i), out);
        }
        out.endObject();
      }
    }

    private String keyToString(JsonElement keyElement) {
      if (keyElement.isJsonPrimitive()) {
        JsonPrimitive primitive = keyElement.getAsJsonPrimitive();
        if (primitive.isNumber()) {
          return String.valueOf(primitive.getAsNumber());
        } else if (primitive.isBoolean()) {
          return Boolean.toString(primitive.getAsBoolean());
        } else if (primitive.isString()) {
          return primitive.getAsString();
        } else {
          throw new AssertionError();
        }
      } else if (keyElement.isJsonNull()) {
        return "null";
      } else {
        throw new AssertionError();
      }
    }
  }
}
