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

import static com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

import am.yagson.refs.*;
import am.yagson.types.AdapterUtils;
import am.yagson.types.TypeInfoPolicy;
import am.yagson.types.TypeUtils;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Type adapter that reflects over the fields and methods of a class.
 */
public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
  private final ConstructorConstructor constructorConstructor;
  private final FieldNamingStrategy fieldNamingPolicy;
  private final Excluder excluder;

  public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor,
      FieldNamingStrategy fieldNamingPolicy, Excluder excluder) {
    this.constructorConstructor = constructorConstructor;
    this.fieldNamingPolicy = fieldNamingPolicy;
    this.excluder = excluder;
  }

  public boolean excludeField(Field f, boolean serialize) {
    return excludeField(f, serialize, excluder);
  }

  static boolean excludeField(Field f, boolean serialize, Excluder excluder) {
    return !excluder.excludeClass(f.getType(), serialize) && !excluder.excludeField(f, serialize);
  }

  /** first element holds the default name */
  private List<String> getFieldNames(Field f) {
    return getFieldName(fieldNamingPolicy, f);
  }
  
  /** first element holds the default name */
  static List<String> getFieldName(FieldNamingStrategy fieldNamingPolicy, Field f) {
    SerializedName serializedName = f.getAnnotation(SerializedName.class);
    List<String> fieldNames = new LinkedList<String>();
    if (serializedName == null) {
      fieldNames.add(fieldNamingPolicy.translateName(f));
    } else {
      fieldNames.add(serializedName.value());
      for (String alternate : serializedName.alternate()) {
        fieldNames.add(alternate);
      }
    }
    return fieldNames;
  }

  public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
    Class<? super T> raw = type.getRawType();

    if (!Object.class.isAssignableFrom(raw)) {
      return null; // it's a primitive!
    }

    ObjectConstructor<T> constructor = constructorConstructor.get(type);
    return new Adapter<T>(gson, type.getType(), constructor, getBoundFields(gson, type, raw));
  }

  private ReflectiveTypeAdapterFactory.BoundField createBoundField(
      final Gson context, final Field field, final String name,
      final TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
    final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
    // special casing primitives here saves ~5% on Android...
    return new ReflectiveTypeAdapterFactory.BoundField(name, serialize, deserialize) {
      final TypeAdapter<?> typeAdapter = getFieldAdapter(context, field, fieldType);
      @SuppressWarnings({"unchecked", "rawtypes"}) // the type adapter and field type always agree
      @Override void write(JsonWriter writer, Object value, ReferencesWriteContext rctx, boolean typeInfoEmitted)
          throws IOException, IllegalAccessException {
        Object fieldValue = field.get(value);
        TypeAdapter t =
          new TypeAdapterRuntimeTypeWrapper(context, this.typeAdapter, fieldType.getType(),
                  typeInfoEmitted ? TypeUtils.TYPE_INFO_SKIP : TypeUtils.getEmitTypeInfoRule(context, false));
        rctx.doWrite(fieldValue, t, name, writer);
      }
      @SuppressWarnings({ "rawtypes", "unchecked" })
      @Override void read(JsonReader reader, final Object value, Class<?> fieldAdvisedClass, ReferencesReadContext rctx)
          throws IOException, IllegalAccessException {
        TypeAdapter<?> fieldTypeAdapter = typeAdapter; 
        if (fieldAdvisedClass != null) {
          if (AdapterUtils.isTypeAdvisable(fieldTypeAdapter) && !AdapterUtils.isReflective(fieldTypeAdapter)) {
            // this adapter can handle type advices, use it
            TypeAdvisableComplexTypeAdapter complexTypeAdapter = AdapterUtils.toTypeAdvisable(fieldTypeAdapter);
            fieldTypeAdapter = complexTypeAdapter.new Delegate(fieldAdvisedClass, context);
          } else {
            // ignore default adapter for the declared type, as we have the advised class
            fieldTypeAdapter = context.getAdapter(fieldAdvisedClass);
          }
        }
        Object fieldValue = rctx.doRead(reader, fieldTypeAdapter, name);
        ReferencePlaceholder<Object> fieldValuePlaceholder;
        if (fieldValue == null && ((fieldValuePlaceholder = rctx.consumeLastPlaceholderIfAny()) != null)) {
          fieldValuePlaceholder.registerUse(new PlaceholderUse<Object>() {
            public void applyActualObject(Object actualObject) throws IOException {
              try {
                field.set(value, actualObject);
              } catch (IllegalAccessException e) {
                throw new AssertionError(e);
              }
            }
          });
        } else if (fieldValue != null || !isPrimitive) {
          field.set(value, fieldValue);
        }
      }
      public boolean writeField(Object value, ReferencesWriteContext rctx) throws IOException, IllegalAccessException {
        if (!serialized) return false;
        
        // circular references are allowed by YaGson in all reference policies except DISABLED
        if (rctx.getPolicy() == ReferencesPolicy.DISABLED) {
          Object fieldValue = field.get(value);
          return fieldValue != value; // avoid recursion for example for Throwable.cause
        } else {
          return true;
        }
      }
      boolean writeTypeInfoIfNeeded(JsonWriter writer, Object value) throws IOException, IllegalAccessException {
        if (context.getTypeInfoPolicy() == TypeInfoPolicy.EMIT_WRAPPERS_OR_VTYPES) {
          Object fieldValue = field.get(value);
          if (fieldValue != null && TypeUtils.isTypeInfoRequired(fieldValue.getClass(), fieldType.getRawType(), false)) {
            writer.name("@vtype");
            writer.value(fieldValue.getClass().getName());
            return true;
          }
        }
        return false;
      }
    };
  }

  private TypeAdapter<?> getFieldAdapter(Gson gson, Field field, TypeToken<?> fieldType) {
    JsonAdapter annotation = field.getAnnotation(JsonAdapter.class);
    if (annotation != null) {
      TypeAdapter<?> adapter = getTypeAdapter(constructorConstructor, gson, fieldType, annotation);
      if (adapter != null) return adapter;
    }
    return gson.getAdapter(fieldType);
  }

  private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw) {
    Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
    if (raw.isInterface()) {
      return result;
    }

    Type declaredType = type.getType();
    while (raw != Object.class) {
      Field[] fields = raw.getDeclaredFields();
      for (Field field : fields) {
        boolean serialize = excludeField(field, true);
        boolean deserialize = excludeField(field, false);
        if (!serialize && !deserialize) {
          continue;
        }
        field.setAccessible(true);
        Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
        List<String> fieldNames = getFieldNames(field);
        BoundField previous = null;
        for (int i = 0; i < fieldNames.size(); ++i) {
          String name = fieldNames.get(i);
          if (i != 0) serialize = false; // only serialize the default name
          BoundField boundField = createBoundField(context, field, name,
              TypeToken.get(fieldType), serialize, deserialize);
          BoundField replaced = result.put(name, boundField);
          if (previous == null) previous = replaced;
        }
        if (previous != null) {
          throw new IllegalArgumentException(declaredType
              + " declares multiple JSON fields named " + previous.name);
        }
      }
      type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
      raw = type.getRawType();
    }
    return result;
  }

  static abstract class BoundField {
    final String name;
    final boolean serialized;
    final boolean deserialized;

    protected BoundField(String name, boolean serialized, boolean deserialized) {
      this.name = name;
      this.serialized = serialized;
      this.deserialized = deserialized;
    }
    abstract boolean writeField(Object value, ReferencesWriteContext rctx) throws IOException, IllegalAccessException;
    abstract void write(JsonWriter writer, Object value, ReferencesWriteContext ctx, boolean typeInfoEmitted) throws IOException, IllegalAccessException;
    abstract void read(JsonReader reader, Object value, Class<?> fieldAdvisedClass, ReferencesReadContext rctx) throws IOException, IllegalAccessException;
    abstract boolean writeTypeInfoIfNeeded(JsonWriter writer, Object value) throws IOException, IllegalAccessException;
  }

  public static final class Adapter<T> extends TypeAdvisableComplexTypeAdapter<T> {
    private final Gson gson;
    private final ObjectConstructor<T> constructor;
    private final Map<String, BoundField> boundFields;
    private final Type objType;

    private Adapter(Gson gson, Type objType, ObjectConstructor<T> constructor, Map<String, BoundField> boundFields) {
      this.gson = gson;
      this.objType = objType;
      this.constructor = constructor;
      this.boundFields = boundFields;
    }

    @Override
    protected T readOptionallyAdvisedInstance(T advisedInstance, JsonReader in, ReferencesReadContext rctx) throws IOException {
      try {
        in.beginObject();

        T instance = null;

        Class<?> nextFieldAdvisedClass = null;
        while (in.hasNext()) {
          String name = in.nextName();
          if (name.equals("@type") && instance == null) {
            return TypeUtils.readTypeAdvisedValueAfterTypeField(gson, in, objType, rctx);
          }

          if (instance == null) {
            instance = getInstance(advisedInstance, rctx);
          }
          if (name.equals("@vtype")) {
            String advisedTypeStr = in.nextString();
            nextFieldAdvisedClass = Class.forName(advisedTypeStr);
          } else {
            BoundField field = boundFields.get(name);
            if (field == null || !field.deserialized) {
              in.skipValue();
            } else {
              field.read(in, instance, nextFieldAdvisedClass, rctx);
            }
            nextFieldAdvisedClass = null;
          }
        }
        if (instance == null) {
          instance = getInstance(advisedInstance, rctx);
        }
        in.endObject();
        return instance;
      } catch (IllegalStateException e) {
        throw new JsonSyntaxException(e);
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      } catch (ClassNotFoundException e) {
        throw new JsonSyntaxException("Missing class specified in @vtype info", e);
      }
    }

    private T getInstance(T advisedInstance, ReferencesReadContext rctx) {
      T instance = advisedInstance == null ? constructor.construct() : advisedInstance;
      rctx.registerObject(instance, false);
      return instance;
    }

    @Override public void write(JsonWriter out, T value, ReferencesWriteContext rctx) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }

      out.beginObject();
      try {
        for (BoundField boundField : boundFields.values()) {
          if (boundField.writeField(value, rctx)) {
            boolean typeInfoEmitted = boundField.writeTypeInfoIfNeeded(out, value);
            out.name(boundField.name);
            boundField.write(out, value, rctx, typeInfoEmitted);
          }
        }
      } catch (IllegalAccessException e) {
        throw new AssertionError();
      }
      out.endObject();
    }
  }
}
