/*
 * Copyright (C) 2011 Google Inc.
 * Modifications copyright (C) 2016 Andrey Mogilev
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

import static com.gilecode.yagson.refs.References.REF_FIELD_PREFIX;
import static com.gilecode.yagson.refs.References.keyRef;
import static com.gilecode.yagson.refs.References.valRef;
import static com.gilecode.yagson.types.TypeUtils.classOf;
import static com.gilecode.yagson.types.TypeUtils.classes;

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;
import com.gilecode.yagson.adapters.AdapterUtils;
import com.gilecode.yagson.adapters.TypeAdvisableComplexTypeAdapter;
import com.gilecode.yagson.refs.*;
import com.gilecode.yagson.refs.impl.MapPlaceholderUse;
import com.gilecode.yagson.types.*;

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
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    if (requireReflectiveAdapter(gson, rawType)) {
      // with type info, if a Map impl class contains another field, it shall be serialized as general class
      return null;
    }

    Type[] keyAndValueTypes = $Gson$Types.getMapKeyAndValueTypes(type, rawType);
    TypeAdapter<?> keyAdapter = getKeyAdapter(gson, keyAndValueTypes[0]);
    TypeAdapter<?> valueAdapter = gson.getAdapter(TypeToken.get(keyAndValueTypes[1]));
    ObjectConstructor<T> constructor = constructorConstructor.get(typeToken);

    @SuppressWarnings({"unchecked", "rawtypes"})
    // we don't define a type parameter for the key or value types
    TypeAdapter<T> result = new Adapter(gson, type, keyAndValueTypes[0], keyAdapter,
        keyAndValueTypes[1], valueAdapter, constructor);
    return result;
  }

  /**
   * Determines whether the specified map type shall be processed by a {@link ReflectiveTypeAdapterFactory}
   * rather than as general maps. This is required to support special and delegate maps like unmodifiableMap
   * when the type info is enabled.
   * <p/>
   * The current rules: a map shall be processed by reflective type adapter if it contains at least one
   * non-transient Map field (except of Properties, which may be added as extra field), or does not override
   * AbstractMap.put() (if extends AbstractMap).
   */
  private static boolean requireReflectiveAdapter(Gson gson, Class<?> mapType) {
    try {
      return gson.getTypeInfoPolicy().isEnabled() && TypeUtils.isGeneralNonAbstractClass(mapType) &&
              (TypeUtils.containsField(mapType, false, classOf(Map.class), classOf(Properties.class)) ||
                      (AbstractMap.class.isAssignableFrom(mapType) && !TypeUtils.isOverridden(mapType,
                              AbstractMap.class.getDeclaredMethod("put", Object.class, Object.class))));
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Returns a type adapter that writes the value as a string.
   */
  private TypeAdapter<?> getKeyAdapter(Gson context, Type keyType) {
    return (keyType == boolean.class || keyType == Boolean.class)
        ? TypeAdapters.BOOLEAN_AS_STRING
        : context.getAdapter(TypeToken.get(keyType));
  }

  public final class Adapter<K, V> extends TypeAdvisableComplexTypeAdapter<Map<K, V>> {
    private final TypeAdapter<K> keyTypeAdapter;
    private final TypeAdapter<V> valueTypeAdapter;
    private final Class<?> rawKeyType;
    private final ObjectConstructor<? extends Map<K, V>> constructor;
    private final Gson gson;
    private final Type formalMapType; // declared map type, e.g. SortedMap<String, String>
    private final Class<? extends Map> defaultMapClass; // raw class of the default map instance (defined by constructor)
    private final Map<K, V> defaultMapInstance; // the default Map instance, as returned by the constructor
    private final Field modCountField; // modCount field found in the default map class, if any
    private final Map<String, FieldInfo> reflectiveFields; // reflective (extra) fields for raw map type

    public Adapter(Gson gson, Type formalMapType, Type keyType, TypeAdapter<K> keyTypeAdapter,
                   Type valueType, TypeAdapter<V> valueTypeAdapter,
                   ObjectConstructor<? extends Map<K, V>> constructor) {
      this.keyTypeAdapter =
        new TypeAdapterRuntimeTypeWrapper<K>(gson, keyTypeAdapter, keyType,
                TypeUtils.getEmitTypeInfoRule(gson, true));
      this.valueTypeAdapter =
        new TypeAdapterRuntimeTypeWrapper<V>(gson, valueTypeAdapter, valueType,
                TypeUtils.getEmitTypeInfoRule(gson, false));
      this.formalMapType = formalMapType;
      this.rawKeyType = $Gson$Types.getRawType(keyType);
      this.constructor = constructor;
      this.gson = gson;

      this.defaultMapInstance = constructor.construct();
      if (defaultMapInstance != null) {
        this.defaultMapClass = defaultMapInstance.getClass();
        this.reflectiveFields = buildReflectiveFieldsInfo(defaultMapClass,
                new ExistingObjectProvider(defaultMapInstance));
      } else {
        this.defaultMapClass = null;
        this.reflectiveFields = null;
      }
      this.modCountField = getModCountField(defaultMapClass);
    }

    private Field getModCountField(Class<?> mapClass) {
      return TypeUtils.findField(mapClass, "modCount");
    }

    public Type getFormalMapType() {
      return formalMapType;
    }

    private Map<String, FieldInfo> buildReflectiveFieldsInfo(Class<? extends Map> containerClass,
                                                             ObjectProvider<? extends Map> objectProvider) {
      return AdapterUtils.buildReflectiveFieldsInfo(gson, containerClass,
              objectProvider, classes(Comparator.class, Properties.class), null);
    }

    @Override
    protected Map<K, V> readOptionallyAdvisedInstance(JsonReader in,
                                                      ReadContext ctx) throws IOException {

      Map<K, V> instance = null;

      // supplementary list, for each map's entry contains either the key, or its placeholder (until resolved)
      List<Object> keys = new ArrayList<Object>();
      // supplementary list, for each map's entry contains either the value, or its placeholder (until resolved)
      List<Object> values = new ArrayList<Object>();

      JsonToken peek = in.peek();

      // modCount field for the actual map class; created only if map is not empty
      final Field localModCountField;

      if (peek == JsonToken.BEGIN_ARRAY) {
        instance = constructor.construct();
        ctx.registerObject(instance, false);

        in.beginArray();
        localModCountField = !in.hasNext() ? null :
                (defaultMapClass == instance.getClass() ? modCountField : getModCountField(instance.getClass()));

        for (int i = 0; in.hasNext(); i++) {
          if (in.peek() == JsonToken.BEGIN_OBJECT) {
            // extra fields object found
            in.beginObject();

            Map<String, FieldInfo> localReflectiveFields = defaultMapClass == instance.getClass() ? reflectiveFields :
                    buildReflectiveFieldsInfo(instance.getClass(), new ExistingObjectProvider(instance));
            // loop until all extra fields are read
            for ( ; in.peek() == JsonToken.NAME; i++) {
              String fieldRef = in.nextName();
              if (!fieldRef.startsWith(REF_FIELD_PREFIX)) {
                throw new JsonSyntaxException("@.field expected, but got: '" + fieldRef + "'");
              }
              // save the read field into the instance
              AdapterUtils.readAndSetReflectiveField(instance, fieldRef.substring(REF_FIELD_PREFIX.length()),
                      localReflectiveFields, in, ctx, valRef(i));
            }
            in.endObject();
            continue;
          }

          ReferencePlaceholder<K> keyPlaceholder = null;
          ReferencePlaceholder<V> valuePlaceholder = null;

          in.beginArray(); // entry array
          K key = ctx.doRead(in, keyTypeAdapter, keyRef(i));
          if (key == null && (keyPlaceholder = ctx.refsContext().consumeLastPlaceholderIfAny()) != null) {
            keys.add(keyPlaceholder);
            PlaceholderUse<K> keyUse = MapPlaceholderUse.keyUse(instance, keys, values, i, modCountField);
            keyPlaceholder.registerUse(keyUse);
          } else {
            keys.add(key);
          }

          V value = ctx.doRead(in, valueTypeAdapter, valRef(i));
          if (value == null && (valuePlaceholder = ctx.refsContext().consumeLastPlaceholderIfAny()) != null) {
            values.add(valuePlaceholder);
            PlaceholderUse<V> valueUse = MapPlaceholderUse.valueUse(instance, keys, values, i, modCountField);
            valuePlaceholder.registerUse(valueUse);
          } else {
            values.add(value);
          }
          // the map is filled later, after all key/value pairs are read
          in.endArray();
        }
        in.endArray();
      } else {
        in.beginObject();

        String fieldRef = null;

        if (in.peek() == JsonToken.NAME) {
          // if type is advised with @type, follow that advice
          String keyStr = in.nextName();
          if (keyStr.equals("@type")) {
            return TypeUtils.readTypeAdvisedValueAfterTypeField(gson, in, formalMapType, ctx);
          } else if (keyStr.startsWith(REF_FIELD_PREFIX)) {
            // extra field processing will be performed at first loop iteration
            fieldRef = keyStr;
          } else {
            // return looked ahead name back to the reader's buffer, as string or long
            returnNameToReader(in, keyStr);
          }
        }

        // otherwise use the default instance
        instance = constructor.construct();
        ctx.registerObject(instance, false);
        Map<String, FieldInfo> localReflectiveFields = null;

        localModCountField = !in.hasNext() ? null :
                (defaultMapClass == instance.getClass() ? modCountField : getModCountField(instance.getClass()));

        for (int i = 0; in.hasNext(); i++) {
          // lookup for extra fields ("@.name")
          if (i != 0 && in.peek() == JsonToken.NAME) {
            String keyStr = in.nextName();
            if (keyStr.startsWith(REF_FIELD_PREFIX)) {
              fieldRef = keyStr;
            } else {
              returnNameToReader(in, keyStr);
            }
          }

          if (fieldRef != null) {
            if (localReflectiveFields == null) { // lazy init
              localReflectiveFields = defaultMapClass == instance.getClass() ? reflectiveFields :
                      buildReflectiveFieldsInfo(instance.getClass(), new ExistingObjectProvider(instance));
            }
            // save the read field into the instance
            AdapterUtils.readAndSetReflectiveField(instance, fieldRef.substring(REF_FIELD_PREFIX.length()),
                    localReflectiveFields, in, ctx, valRef(i));

            fieldRef = null;
            continue;
          }

          ReferencePlaceholder<K> keyPlaceholder = null;
          ReferencePlaceholder<V> valuePlaceholder = null;

          K key = ctx.doRead(in, keyTypeAdapter, keyRef(i));
          if (key == null && (keyPlaceholder = ctx.refsContext().consumeLastPlaceholderIfAny()) != null) {
            keys.add(keyPlaceholder);
            PlaceholderUse<K> keyUse = MapPlaceholderUse.keyUse(instance, keys, values, i, modCountField);
            keyPlaceholder.registerUse(keyUse);
          } else {
            keys.add(key);
          }

          V value = ctx.doRead(in, valueTypeAdapter, valRef(i));
          if (value == null && (valuePlaceholder = ctx.refsContext().consumeLastPlaceholderIfAny()) != null) {
            values.add(valuePlaceholder);
            PlaceholderUse<V> valueUse = MapPlaceholderUse.valueUse(instance, keys, values, i, modCountField);
            valuePlaceholder.registerUse(valueUse);
          } else {
            values.add(value);
          }

        }
        in.endObject();
      }

      for (int i = 0; i < keys.size(); i++) {
        Object keyOrPlaceholder = keys.get(i);
        Object valueOrPlaceholder = values.get(i);

        if (keyOrPlaceholder instanceof ReferencePlaceholder || valueOrPlaceholder instanceof ReferencePlaceholder) {
          // this entry is not ready yet, skip it
        } else {
          V replaced = instance.put((K)keyOrPlaceholder, (V)valueOrPlaceholder);
          if (replaced != null) {
            throw new JsonSyntaxException("duplicate key: " + keyOrPlaceholder);
          }
        }
      }
      if (keys.size() > 0 && localModCountField != null) {
        AdapterUtils.clearModCount(localModCountField, instance);
      }

      return instance;
    }

    /**
     * Return the just read string to JsonReader. In all cases except of 'long' value,
     * it may be returned as BUFFERED string, but long literals shall be returned as longs.
     */
    private void returnNameToReader(JsonReader in, String readName) throws IOException {
      boolean longReturned = false;
      if (rawKeyType.equals(Long.class) || rawKeyType.equals(Number.class)) {
        Long val = TypeUtils.parseNumberIfLong(readName);
        if (val != null) {
          JsonReaderInternalAccess.INSTANCE.returnLongToBuffer(in, val.longValue());
          longReturned = true;
        }
      }
      if (!longReturned) {
        // for other cases (including boolean and double) return as string
        JsonReaderInternalAccess.INSTANCE.returnStringToBuffer(in, readName);
      }
    }

    private void writeExtraFields(JsonWriter out, Map<K, V> map, WriteContext ctx, int startIdx,
                                  boolean wrapInObject) throws IOException {
      Map<String, FieldInfo> localReflectiveFields = defaultMapClass == map.getClass() ? reflectiveFields :
              buildReflectiveFieldsInfo(map.getClass(), new ConstructingObjectProvider(
                      constructorConstructor.get(TypeToken.get(map.getClass()))));
      final AtomicInteger mapEntryIdx = new AtomicInteger(startIdx);
      AdapterUtils.writeReflectiveFields(map, localReflectiveFields, out, ctx, new PathElementProducer() {
        public String produce() {
          int i = mapEntryIdx.getAndIncrement();
          return "" + i + "-val";
        }
      }, wrapInObject);
    }

    public void write(JsonWriter out, Map<K, V> map, WriteContext ctx) throws IOException {
      if (map == null) {
        out.nullValue();
        return;
      }

      if (defaultMapClass != map.getClass() && requireReflectiveAdapter(gson, map.getClass())) {
        // although the condition was checked in the factory for the formal type, it shall be
        // re-checked for the actual class if it differs

        AdapterUtils.writeByReflectiveAdapter(out, map, ctx, gson, formalMapType);
        return;
      }

      if (!complexMapKeySerialization) {
        out.beginObject();
        int i = 0;
        for (Map.Entry<K, V> entry : map.entrySet()) {
          out.name(String.valueOf(entry.getKey()));
          ctx.doWrite(entry.getValue(), valueTypeAdapter, "" + i + "-val", out);
          i++;
        }
        writeExtraFields(out, map, ctx, i, false);
        out.endObject();
        return;
      }

      boolean skipEntries = ctx.isSkipNextMapEntries();
      boolean hasComplexKeys = false;
      List<JsonElement> keys = new ArrayList<JsonElement>(map.size());
      List<V> values = new ArrayList<V>(map.size());
      int i = 0;

      if (!skipEntries) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
          JsonElement keyElement = ctx.doToJsonTree(entry.getKey(), keyTypeAdapter, keyRef(i));
          i++;
          keys.add(keyElement);
          values.add(entry.getValue());
          hasComplexKeys |= keyElement.isJsonArray() || keyElement.isJsonObject();
        }
      }

      if (hasComplexKeys) {
        out.beginArray();
        if (!skipEntries) {
          for (i = 0; i < keys.size(); i++) {
            out.beginArray(); // entry array
            Streams.write(keys.get(i), out);
            ctx.doWrite(values.get(i), valueTypeAdapter, valRef(i), out);
            out.endArray();
          }
        }
        writeExtraFields(out, map, ctx, keys.size(), true);
        out.endArray();
      } else {
        out.beginObject();
        if (!skipEntries) {
          for (i = 0; i < keys.size(); i++) {
            JsonElement keyElement = keys.get(i);
            out.name(keyToString(keyElement));
            ctx.doWrite(values.get(i), valueTypeAdapter, valRef(i), out);
          }
        }
        writeExtraFields(out, map, ctx, keys.size(), false);
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
