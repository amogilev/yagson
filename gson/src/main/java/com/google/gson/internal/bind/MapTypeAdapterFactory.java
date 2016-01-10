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

import static am.yagson.refs.References.REF_FIELD_PREFIX;
import static am.yagson.refs.References.keyRef;
import static am.yagson.refs.References.valRef;
import static am.yagson.types.AdapterUtils.isTypeAdvisable;
import static am.yagson.types.AdapterUtils.toTypeAdvisable;
import static am.yagson.types.TypeUtils.classOf;
import static am.yagson.types.TypeUtils.classes;

import am.yagson.refs.*;

import am.yagson.refs.impl.MapPlaceholderUse;
import am.yagson.types.*;
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
  private boolean requireReflectiveAdapter(Gson gson, Class<?> mapType) {
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

  private final class Adapter<K, V> extends TypeAdvisableComplexTypeAdapter<Map<K, V>> {
    private final TypeAdapter<K> keyTypeAdapter;
    private final TypeAdapter<V> valueTypeAdapter;
    private final Class<?> rawKeyType;
    private final ObjectConstructor<? extends Map<K, V>> constructor;
    private final Gson gson;
    private final Type formalMapType; // declared map type, e.g. SortedMap>String, String>
    private final Class<? extends Map> defaultMapClass; // raw class of the default map instance (defined by constructor)
    private final Map<K, V> defaultMapInstance; // the default Map instance, as returned by the constructor
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
      // this.mapType = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, keyType, valueType);
      this.formalMapType = formalMapType;
      this.rawKeyType = $Gson$Types.getRawType(keyType);
      this.constructor = constructor;
      this.gson = gson;

      this.defaultMapInstance = constructor.construct();
      this.defaultMapClass = defaultMapInstance.getClass();
      this.reflectiveFields = buildReflectiveFieldsInfo(defaultMapClass,
              new ExistingObjectProvider(defaultMapInstance));
    }

    private Map<String, FieldInfo> buildReflectiveFieldsInfo(Class<? extends Map> containerClass,
                                                             ObjectProvider<? extends Map> objectProvider) {
      return AdapterUtils.buildReflectiveFieldsInfo(gson, containerClass, formalMapType,
              objectProvider, classes(Comparator.class, Properties.class), null);
    }

    private void readAndSetField(String fname, Map<String, FieldInfo> reflectiveFields, JsonReader in,
                                 ReferencesReadContext rctx, Map<K, V> mapInstance, String valPathElement) throws IOException {
      FieldInfo fieldInfo = reflectiveFields.get(fname);
      if (fieldInfo == null) {
        throw new JsonSyntaxException("The Map type for " + mapInstance.getClass() +
                " does not have serializable reflective field '" + fname +"'");
      }
      // read value using the corresponding type adapter
      Object fieldValue = rctx.doRead(in, fieldInfo.getFieldAdapter(), valPathElement);

      // try set the field
      try {
        fieldInfo.getField().set(mapInstance, fieldValue);
      } catch (IllegalAccessException e) {
        throw new JsonSyntaxException("Failed to set the Map reflective field value; mapType=" + mapInstance.getClass() +
                "; field=" + fieldInfo.getField() + "; value=" + fieldValue);
      }
    }


    @Override
    protected Map<K, V> readOptionallyAdvisedInstance(Map<K, V> advisedInstance, JsonReader in,
                                                 ReferencesReadContext rctx) throws IOException {

      if (advisedInstance != null && defaultMapClass != advisedInstance.getClass() &&
              requireReflectiveAdapter(gson, advisedInstance.getClass())) {
        // use another (i.e. reflective) type adapter
        TypeToken<Map<K, V>> typeToken = (TypeToken<Map<K, V>>)
                TypeToken.get(TypeUtils.getParameterizedType(advisedInstance.getClass(), formalMapType));
        TypeAdapter<Map<K, V>> adapter = gson.getAdapter(typeToken);
        if (isTypeAdvisable(adapter)) {
          return toTypeAdvisable(adapter).readOptionallyAdvisedInstance(advisedInstance, in, rctx);
        } else {
          return gson.getAdapter(typeToken).read(in, rctx);
        }
      }

      Map<K, V> instance = null;

      // supplementary list, for each map's entry contains either the key, or its placeholder (until resolved)
      List<Object> keys = new ArrayList<Object>();
      // supplementary list, for each map's entry contains either the value, or its placeholder (until resolved)
      List<Object> values = new ArrayList<Object>();

      JsonToken peek = in.peek();

      if (peek == JsonToken.BEGIN_ARRAY) {
        instance = advisedInstance == null ? constructor.construct() : advisedInstance;
        rctx.registerObject(instance);

        in.beginArray();
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
              readAndSetField(fieldRef.substring(REF_FIELD_PREFIX.length()),
                      localReflectiveFields, in, rctx, instance, valRef(i));
            }
            in.endObject();
            continue;
          }

          ReferencePlaceholder<K> keyPlaceholder = null;
          ReferencePlaceholder<V> valuePlaceholder = null;

          in.beginArray(); // entry array
          K key = rctx.doRead(in, keyTypeAdapter, keyRef(i));
          if (key == null && (keyPlaceholder = rctx.consumeLastPlaceholderIfAny()) != null) {
            keys.add(keyPlaceholder);
            PlaceholderUse<K> keyUse = MapPlaceholderUse.keyUse(instance, keys, values, i);
            keyPlaceholder.registerUse(keyUse);
          } else {
            keys.add(key);
          }

          V value = rctx.doRead(in, valueTypeAdapter, valRef(i));
          if (value == null && (valuePlaceholder = rctx.consumeLastPlaceholderIfAny()) != null) {
            values.add(valuePlaceholder);
            PlaceholderUse<V> valueUse = MapPlaceholderUse.valueUse(instance, keys, values, i);
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
            return TypeUtils.readTypeAdvisedValueAfterTypeField(gson, in, formalMapType, rctx);
          } else if (keyStr.startsWith(REF_FIELD_PREFIX)) {
            // extra field processing will be performed at first loop iteration
            fieldRef = keyStr;
          } else {
            // return looked ahead name back to the reader's buffer, as string or long
            returnNameToReader(in, keyStr);
          }
        }

        // otherwise use the previous @vtype advice, or the default instance
        instance = advisedInstance == null ? constructor.construct() : advisedInstance;
        rctx.registerObject(instance);
        Map<String, FieldInfo> localReflectiveFields = null;


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
            readAndSetField(fieldRef.substring(REF_FIELD_PREFIX.length()),
                    localReflectiveFields, in, rctx, instance, valRef(i));

            fieldRef = null;
            continue;
          }

          ReferencePlaceholder<K> keyPlaceholder = null;
          ReferencePlaceholder<V> valuePlaceholder = null;

          K key = rctx.doRead(in, keyTypeAdapter, keyRef(i));
          if (key == null && (keyPlaceholder = rctx.consumeLastPlaceholderIfAny()) != null) {
            keys.add(keyPlaceholder);
            PlaceholderUse<K> keyUse = MapPlaceholderUse.keyUse(instance, keys, values, i);
            keyPlaceholder.registerUse(keyUse);
          } else {
            keys.add(key);
          }

          V value = rctx.doRead(in, valueTypeAdapter, valRef(i));
          if (value == null && (valuePlaceholder = rctx.consumeLastPlaceholderIfAny()) != null) {
            values.add(valuePlaceholder);
            PlaceholderUse<V> valueUse = MapPlaceholderUse.valueUse(instance, keys, values, i);
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
        if (i == 0 && instance instanceof EnumMap) {
          // keys in EnumMap are never placeholders
          assert !(keyOrPlaceholder instanceof ReferencePlaceholder);
          initEnumMapKeyType(instance, keyOrPlaceholder.getClass());
        }
        if (keyOrPlaceholder instanceof ReferencePlaceholder || valueOrPlaceholder instanceof ReferencePlaceholder) {
          // this entry is not ready yet, skip it
        } else {
          V replaced = instance.put((K)keyOrPlaceholder, (V)valueOrPlaceholder);
          if (replaced != null) {
            throw new JsonSyntaxException("duplicate key: " + keyOrPlaceholder);
          }
        }
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

    private void initEnumMapKeyType(Map<K, V> instance, Class<?> keyType) {
      if (!keyType.isEnum()) {
        throw new JsonSyntaxException("Only enum keys are allowed for EnumMap, but got " + keyType);
      }
      EnumMap otherInstance = new EnumMap(keyType);
      copyFields(instance, otherInstance, EnumMap.class, "keyType", "keyUniverse", "vals");
    }

    private void copyFields(Object to, Object from, Class<?> declaringClass, String...fieldNames) {
      for (String fname : fieldNames) {
          try {
            Field f = declaringClass.getDeclaredField(fname);
            f.setAccessible(true);
            f.set(to, f.get(from));
          } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize field " + fname + " of " + declaringClass);
          }
        }
    }

    private int writeExtraFields(JsonWriter out, Map<K, V> map, ReferencesWriteContext rctx, int startIdx,
                                 boolean wrapInObject) throws IOException {
      int i = startIdx;
      Map<String, FieldInfo> localReflectiveFields = defaultMapClass == map.getClass() ? reflectiveFields :
              buildReflectiveFieldsInfo(map.getClass(), new ConstructingObjectProvider(
                      constructorConstructor.get(TypeToken.get(map.getClass()))));
      boolean hasExtraFieldsWritten = false;
      for (FieldInfo f : localReflectiveFields.values()) {
        Object fieldValue;
        try {
          fieldValue = f.getField().get(map);
        } catch (IllegalAccessException e) {
          throw new IOException("Failed to read field", e);
        }
        // skip default values
        if (fieldValue != f.getDefaultValue() && (fieldValue == null || !fieldValue.equals(f.getDefaultValue()))) {
          if (wrapInObject && !hasExtraFieldsWritten) {
            out.beginObject();
          }
          out.name(REF_FIELD_PREFIX + f.getField().getName());
          rctx.doWrite(fieldValue, f.getFieldAdapter(), "" + i + "-val", out);
          hasExtraFieldsWritten = true;
        }
      }
      if (wrapInObject && hasExtraFieldsWritten) {
        out.endObject();
      }
      return i;
    }

    public void write(JsonWriter out, Map<K, V> map, ReferencesWriteContext rctx) throws IOException {
      if (map == null) {
        out.nullValue();
        return;
      }

      if (defaultMapClass != map.getClass() && requireReflectiveAdapter(gson, map.getClass())) {
        // although the condition was checked in the factory for the formal type, it shall be
        // re-checked for the actual class if it differs

        TypeToken<Map<K, V>> typeToken = (TypeToken<Map<K, V>>)
                TypeToken.get(TypeUtils.getParameterizedType(map.getClass(), formalMapType));
        TypeAdapter<Map<K, V>> adapter = gson.getAdapter(typeToken);
        adapter.write(out, map, rctx);
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
        writeExtraFields(out, map, rctx, i, false);
        out.endObject();
        return;
      }

      boolean hasComplexKeys = false;
      List<JsonElement> keys = new ArrayList<JsonElement>(map.size());

      List<V> values = new ArrayList<V>(map.size());
      int i = 0;
      for (Map.Entry<K, V> entry : map.entrySet()) {
        JsonElement keyElement = rctx.doToJsonTree(entry.getKey(), keyTypeAdapter, keyRef(i));
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
        writeExtraFields(out, map, rctx, keys.size(), true);
        out.endArray();
      } else {
        out.beginObject();
        for (i = 0; i < keys.size(); i++) {
          JsonElement keyElement = keys.get(i);
          out.name(keyToString(keyElement));
          rctx.doWrite(values.get(i), valueTypeAdapter, valRef(i), out);
        }
        writeExtraFields(out, map, rctx, keys.size(), false);
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
