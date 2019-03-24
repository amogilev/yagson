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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;
import com.gilecode.yagson.adapters.HasField;
import com.gilecode.yagson.adapters.TypeAdvisableComplexTypeAdapter;
import com.gilecode.yagson.adapters.TypeInfoEmittingTypeAdapterWrapper;
import com.gilecode.yagson.refs.*;
import com.gilecode.yagson.refs.impl.PlaceholderUtils;
import com.gilecode.yagson.types.PostReadProcessor;
import com.gilecode.yagson.types.TypeUtils;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.*;
import com.google.gson.internal.reflect.ReflectionAccessor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Type adapter that reflects over the fields and methods of a class.
 */
public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
  private final ConstructorConstructor constructorConstructor;
  private final FieldNamingStrategy fieldNamingPolicy;
  private final Excluder excluder;
  private final Map<String, PostReadProcessor> readPostProcessorsByClassName;
  private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;
  private final ReflectionAccessor accessor = ReflectionAccessor.getInstance();

  public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor,
      FieldNamingStrategy fieldNamingPolicy, Excluder excluder,
      JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory,
      List<PostReadProcessor> processors) {
    this.constructorConstructor = constructorConstructor;
    this.fieldNamingPolicy = fieldNamingPolicy;
    this.excluder = excluder;
    this.jsonAdapterFactory = jsonAdapterFactory;
    readPostProcessorsByClassName = buildReflectiveAdapterPostReadProcessorsMap(processors);
  }

  private static Map<String, PostReadProcessor> buildReflectiveAdapterPostReadProcessorsMap(List<PostReadProcessor> processors) {
    Map<String, PostReadProcessor> map = new HashMap<String, PostReadProcessor>();
    for (PostReadProcessor p : processors) {
      for (String className : p.getNamesOfProcessedClasses()) {
        map.put(className, p);
      }
    }
    return map;
  }

  public boolean excludeField(Field f, boolean serialize) {
    return excludeField(f, serialize, excluder);
  }

  static boolean excludeField(Field f, boolean serialize, Excluder excluder) {
    return !excluder.excludeClass(f.getType(), serialize) && !excluder.excludeField(f, serialize);
  }

  /** first element holds the default name */
  private List<String> getFieldNames(Field f) {
    SerializedName annotation = f.getAnnotation(SerializedName.class);
    if (annotation == null) {
      String name = fieldNamingPolicy.translateName(f);
      return Collections.singletonList(name);
    }

    String serializedName = annotation.value();
    String[] alternates = annotation.alternate();
    if (alternates.length == 0) {
      return Collections.singletonList(serializedName);
    }

    List<String> fieldNames = new ArrayList<String>(alternates.length + 1);
    fieldNames.add(serializedName);
    for (String alternate : alternates) {
      fieldNames.add(alternate);
    }
    return fieldNames;
  }

  @Override public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
    Class<? super T> raw = type.getRawType();

    if (!Object.class.isAssignableFrom(raw)) {
      return null; // it's a primitive!
    }

    ObjectConstructor<T> constructor = constructorConstructor.get(type);
    return new Adapter<T>(gson, type.getType(), constructor, getBoundFields(gson, type, raw),
            readPostProcessorsByClassName.get(raw.getName()));
  }

  public <T> TypeAdapter<T> createSpecial(Gson gson, final TypeToken<T> type, List<BoundField> extraFields,
                                          PostReadProcessor postReadProcessor) {
    Class<? super T> raw = type.getRawType();

    if (!Object.class.isAssignableFrom(raw)) {
      return null; // it's a primitive!
    }

    Map<String, BoundField> boundFields = getBoundFields(gson, type, raw);
    for (BoundField extraField : extraFields) {
      boundFields.put(extraField.name, extraField);
    }
    ObjectConstructor<T> constructor = constructorConstructor.get(type);
    return new Adapter<T>(gson, type.getType(), constructor, boundFields, postReadProcessor);
  }

  public static class DefaultBoundField extends BoundField {
    TypeAdapter<?> typeAdapter;
    private final Gson context;
    private final TypeToken<?> fieldType;
    private final boolean isPrimitive;
    private boolean jsonAdapterPresent;

    public DefaultBoundField(String name, Field field, boolean serialize, boolean deserialize, Gson context,
                             TypeToken<?> fieldType, JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory,
                             ConstructorConstructor constructorConstructor) {
      super(name, field, serialize, deserialize);
      this.context = context;
      this.fieldType = fieldType;
      // special casing primitives here saves ~5% on Android...
      this.isPrimitive = Primitives.isPrimitive(fieldType.getRawType());

      if (field != null) {
        JsonAdapter annotation = field.getAnnotation(JsonAdapter.class);
        if (annotation != null) {
          typeAdapter = jsonAdapterFactory.getTypeAdapter(
                  constructorConstructor, context, fieldType, annotation);
        }
      }
      if (typeAdapter != null) {
        jsonAdapterPresent = true;
      } else {
        typeAdapter = context.getAdapter(fieldType);
      }
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // the type adapter and field type always agree
    @Override
    protected void write(JsonWriter writer, Object value, WriteContext ctx)
            throws IOException, IllegalAccessException {

      String pathElement = name;
      Object fieldValue = getFieldValue(value);

      // special support for hashcodes - use '@.hash' reference instead of the value
      if (isHashcode(value, fieldValue)) {
        writer.name(name);
        writer.value(References.REF_HASH);
        return;
      }

      // the resolved (unwrapped) type adapter for the value, without type info emitting.
      // This one is used to check whether the reference is available, and so if the type emitting is needed at all
      TypeAdapter fieldTypeAdapter = jsonAdapterPresent ? typeAdapter : new TypeAdapterRuntimeTypeWrapper(context, this.typeAdapter,
              fieldType.getType(), TypeUtils.TYPE_INFO_SKIP).resolve(fieldValue);

      /* type info will be emitted if:
        - type info is enabled and required (deserialization type is not known)
        - the value will be emitted as the value rather than the reference
       */
      boolean emitTypeInfo = context.getTypeInfoPolicy().isEnabled() && fieldValue != null &&
              TypeUtils.isTypeInfoRequired(fieldValue.getClass(), fieldType.getRawType(), false) &&
              ctx.getReferenceFor(fieldValue, fieldTypeAdapter, pathElement) == null;

      if (emitTypeInfo) {
        // will be emitted as @type/@value
        fieldTypeAdapter = new TypeInfoEmittingTypeAdapterWrapper(fieldTypeAdapter);
      }

      writer.name(name);
      ctx.doWrite(fieldValue, fieldTypeAdapter, pathElement, writer);
    }

    /**
     * Checks whether this field is used to store hash code and its value is the actual hashcode value
     */
    private boolean isHashcode(Object obj, Object fieldValue) {
      if (isPrimitive && context.getReferencesPolicy().isEnabled() && name.equalsIgnoreCase("hashcode") &&
              fieldType.getRawType().equals(int.class) && fieldValue != null && !fieldValue.equals(0)) {
        // looks like hash code field, now check the actual value
        int actualHashCode;
        try {
          actualHashCode = obj.hashCode();
        } catch (Throwable e) {
          // guard against failed hashCode (like NPE)
          return false;
        }
        return fieldValue.equals(actualHashCode);
      }
      return false;
    }

    protected Object getFieldValue(Object value) throws IllegalAccessException {
      return field.get(value);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void read(JsonReader reader, final Object value,
                        ReadContext ctx, Map<Field, ReferencePlaceholder> fieldPlaceholders)
            throws IOException, IllegalAccessException {

      ReferencePlaceholder fieldValuePlaceholder = null;

      // special support for hashcodes and field references
      if (reader.peek() == JsonToken.STRING) {
        String token = reader.nextString();
        if (token.startsWith("@")) {
          if (isPrimitive && name.equalsIgnoreCase("hashcode") &&
                  fieldType.getRawType().equals(int.class)) {
            fieldValuePlaceholder = new HashReferencePlaceholder();
          }
          if (fieldValuePlaceholder != null) {
            applyReadPlaceholder(value, fieldPlaceholders, fieldValuePlaceholder);
            return;
          }
        }
        JsonReaderInternalAccess.INSTANCE.returnStringToBuffer(reader, token);
      }

      Object fieldValue = ctx.doRead(reader, typeAdapter, name);
      if (fieldValue == null && ((fieldValuePlaceholder = ctx.refsContext().consumeLastPlaceholderIfAny()) != null)) {
        applyReadPlaceholder(value, fieldPlaceholders, fieldValuePlaceholder);
      } else {
        applyReadFieldValue(value, fieldValue);
      }
    }

    protected void applyReadPlaceholder(Object value, Map<Field, ReferencePlaceholder> fieldPlaceholders,
                                        ReferencePlaceholder fieldValuePlaceholder) {
      fieldValuePlaceholder.registerUse(new FieldPlaceholderUse<Integer>(field, value));
      fieldPlaceholders.put(field, fieldValuePlaceholder);
    }

    protected void applyReadFieldValue(Object value, Object fieldValue) throws IllegalAccessException {
      if (fieldValue != null || !isPrimitive) {
        field.set(value, fieldValue);
      }
    }

    protected boolean writeField(Object value, WriteContext ctx) throws IOException, IllegalAccessException {
      if (!serialized) return false;

      // circular references are allowed by YaGson in all reference policies except DISABLED
      if (ctx.refsPolicy() == ReferencesPolicy.DISABLED) {
        Object fieldValue = getFieldValue(value);
        return fieldValue != value; // avoid recursion for example for Throwable.cause
      } else {
        return true;
      }
    }
  }

  private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw) {
    Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
    if (raw.isInterface()) {
      return result;
    }

    Type declaredType = type.getType();
    int superLevel = 0;
    while (raw != Object.class) {
      Field[] fields = raw.getDeclaredFields();
      for (Field field : fields) {
        boolean serialize = excludeField(field, true);
        boolean deserialize = excludeField(field, false);
        if (!serialize && !deserialize) {
          continue;
        }
        accessor.makeAccessible(field);
        Type fieldType = $Gson$Types.resolve(type.getType(), raw, TypeUtils.getFieldGenericTypeSafe(field));
        List<String> fieldNames = getFieldNames(field);
        for (int i = 0, size = fieldNames.size(); i < size; ++i) {
          String name = fieldNames.get(i);
          if (i != 0) serialize = false; // only serialize the default name
          if(result.containsKey(name)) {
            name = name + "^" + superLevel;
            if (superLevel == 0 || result.containsKey(name)) {
              throw new IllegalArgumentException(declaredType
                      + " declares multiple JSON fields named " + fieldNames.get(i));
            }
          }
          BoundField boundField = new DefaultBoundField(name, field, serialize, deserialize, context,
                  TypeToken.get(fieldType), jsonAdapterFactory, constructorConstructor);
          result.put(name, boundField);
        }
      }
      type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
      raw = type.getRawType();
      superLevel++;
    }
    return result;
  }

  public static abstract class BoundField implements HasField {
    final String name;
    final Field field;
    final boolean serialized;
    final boolean deserialized;

    protected BoundField(String name, Field field, boolean serialized, boolean deserialized) {
      this.name = name;
      this.field = field;
      this.serialized = serialized;
      this.deserialized = deserialized;
    }
    protected abstract boolean writeField(Object value, WriteContext ctx) throws IOException, IllegalAccessException;
    protected abstract void write(JsonWriter writer, Object value, WriteContext ctx) throws IOException, IllegalAccessException;
    protected abstract void read(JsonReader reader, Object value, ReadContext ctx,
                       Map<Field, ReferencePlaceholder> fieldPlaceholders) throws IOException, IllegalAccessException;

    public Field getField() {
      return field;
    }
  }

  public static final class Adapter<T> extends TypeAdvisableComplexTypeAdapter<T> {
    private final Gson gson;
    private final ObjectConstructor<T> constructor;
    private final Map<String, BoundField> boundFields;
    private final Type objType;
    private final PostReadProcessor postReadProcessor;

    Adapter(Gson gson, Type objType, ObjectConstructor<T> constructor, Map<String, BoundField> boundFields,
                    PostReadProcessor postReadProcessor) {
      this.gson = gson;
      this.objType = objType;
      this.constructor = constructor;
      this.boundFields = boundFields;
      this.postReadProcessor = postReadProcessor;
    }

    @Override
    protected T readOptionallyAdvisedInstance(JsonReader in, ReadContext ctx) throws IOException {
      try {
        in.beginObject();

        T instance = null;
        final Map<Field, ReferencePlaceholder> fieldPlaceholders = new HashMap<Field, ReferencePlaceholder>();

        while (in.hasNext()) {
          String name = in.nextName();
          if (name.equals("@type") && instance == null) {
            return TypeUtils.readTypeAdvisedValueAfterTypeField(gson, in, objType, ctx);
          }

          if (instance == null) {
            instance = getInstance(ctx);
          }
          BoundField field = boundFields.get(name);
          if (field == null || !field.deserialized) {
            in.skipValue();
          } else {
            field.read(in, instance, ctx, fieldPlaceholders);
          }
        }
        if (instance == null) {
          instance = getInstance(ctx);
        }
        if (postReadProcessor != null) {
          postReadProcessor.apply(instance);
        }

        if (!fieldPlaceholders.isEmpty()) {
          // NOTE: may contain FieldReferencePlaceholder if RefsContext fail to resolve them due to re-ordering,
          //    or by implementation (i.e. in CircularAndSibling mode)
          PlaceholderUtils.applyOrDeferHashAndFieldPlaceholders(instance, fieldPlaceholders, boundFields);
        }
        in.endObject();
        return instance;
      } catch (IllegalStateException e) {
        throw new JsonSyntaxException(e);
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }

    private T getInstance(ReadContext ctx) {
      T instance = constructor.construct();
      ctx.registerObject(instance, false);
      return instance;
    }

    @Override public void write(JsonWriter out, T value, WriteContext ctx) throws IOException {
      if (value == null) {
        out.nullValue();
        return;
      }

      out.beginObject();
      try {
        for (BoundField boundField : boundFields.values()) {
          if (boundField.writeField(value, ctx)) {
            boundField.write(out, value, ctx);
          }
        }
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
      out.endObject();
    }
  }

  private static class FieldPlaceholderUse<T> implements PlaceholderUse<T> {
    private final Field field;
    private final Object instance;

    public FieldPlaceholderUse(Field field, Object instance) {
      this.field = field;
      this.instance = instance;
    }

    public void applyActualObject(T actualObject) throws IOException {
      try {
        field.set(instance, actualObject);
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
  }
}
