/*
 * Copyright (C) 2016 Andrey Mogilev
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
package com.gilecode.yagson.adapters;

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;
import com.gilecode.yagson.refs.PathElementProducer;
import com.gilecode.yagson.types.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper;
import com.google.gson.internal.reflect.ReflectionAccessor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

import static com.gilecode.yagson.refs.References.REF_FIELD_PREFIX;

/**
 * Utility methods for working with type adapters and delegates.
 *
 * @author Andrey Mogilev
 */
public class AdapterUtils {

    /**
     * Returns whether the specified type adapter is a special adapter created by {@link Excluder} to exclude classes,
     * or delegates to it.
     */
    public static <T> boolean isSkipSerializeTypeAdapter(TypeAdapter<T> typeAdapter) {
        if (typeAdapter instanceof Excluder.ExcluderTypeAdapter) {
            return ((Excluder.ExcluderTypeAdapter)typeAdapter).isSkipSerialize();
        } else if (typeAdapter instanceof DelegatingTypeAdapter) {
            return isSkipSerializeTypeAdapter(((DelegatingTypeAdapter)typeAdapter).getDelegate());
        } else {
            return false;
        }
    }

    /**
     * Returns the fields which needs to be saved for the special container object (map, set or collection) except
     * of the standard collection entries, if these fields have non-default values in the actual serialized objects.
     * <p/>
     * For example, non-null comparators in maps/sets need to be saved.
     * @param containerClass the class containing the reflective fields; e.g. HashMap.class
     * @param defaultObjectProvider the provider of the default container object for checking the default values
     * @param fieldClassesToFind the field classes to find
     * @param exceptClasses the field classes to skip from search
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, FieldInfo> buildReflectiveFieldsInfo(Gson gson, Class<? extends T> containerClass,
                                                                       ObjectProvider<? extends T> defaultObjectProvider,
                                                                       Iterable<Class<?>> fieldClassesToFind,
                                                                       Iterable<Class<?>> exceptClasses) {
        if (!gson.getTypeInfoPolicy().isEnabled()) {
            // require type info for correct work
            return Collections.emptyMap();
        }

        Map<String, FieldInfo> result = new TreeMap<String, FieldInfo>();
        List<Field> fields = TypeUtils.findFields(containerClass, true, fieldClassesToFind, exceptClasses);
        if (fields.isEmpty()) {
            return result;
        }

        T defaultInstance = defaultObjectProvider.get();

        for (Field f : fields) {
            String fname = f.getName();
            if (result.containsKey(fname)) {
                // skip duplicate names, get/set only 'latest' versions (closest to the actual class)
                continue;
            }
            ReflectionAccessor.getInstance().makeAccessible(f);

            Object defaultValue;
            try {
                defaultValue = f.get(defaultInstance);
            } catch (IllegalAccessException e) {
                throw new JsonSyntaxException("Failed to get the Map reflective field value; mapType=" + containerClass +
                        "; field=" + f);
            }

            Class<?> defaultFieldClass = defaultValue == null ? f.getType() : defaultValue.getClass();
            Type fieldType = TypeUtils.getFieldGenericTypeSafe(f);
            TypeAdapter<Object> fieldAdapter = new DefaultTypeAdapterRuntimeWrapper(gson, defaultFieldClass, fieldType);

            result.put(fname, new FieldInfo(f, defaultValue, fieldAdapter));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> void writeByReflectiveAdapter(JsonWriter out, T obj, WriteContext ctx,
                                                    Gson gson, Type formalType) throws IOException {
        TypeToken<T> typeToken = (TypeToken<T>)
                TypeToken.get(TypeUtils.mergeTypes(obj.getClass(), formalType));
        TypeAdapter<T> adapter = gson.getAdapter(typeToken);
        if (!(adapter instanceof ReflectiveTypeAdapterFactory.Adapter)) {
            adapter = gson.getReflectiveTypeAdapterFactory().create(gson, typeToken);
        }
        adapter.write(out, obj, ctx);
    }

    /**
     * Writes those of reflective fields which actual values does not contain non-serialized lambdas and differs from
     * the default values.
     */
    public static void writeNonDefaultReflectiveFieldsWithNoNSLambdas(Object instance, Map<String, FieldInfo> reflectiveFields,
                                                                      JsonWriter out, WriteContext ctx, PathElementProducer refPathProducer,
                                                                      boolean wrapInObject) throws IOException {
        boolean hasExtraFieldsWritten = false;
        for (FieldInfo f : reflectiveFields.values()) {
            Object fieldValue;
            try {
                fieldValue = f.getField().get(instance);
            } catch (IllegalAccessException e) {
                throw new IOException("Failed to read field", e);
            }
            // skip default values
            if (fieldValue != f.getDefaultValue() && (fieldValue == null || !fieldValue.equals(f.getDefaultValue()))) {
                // do actual write only if does not contain non-serializable lambdas
                WriteContext origCtx = ctx;
                ctx = ctx.makeChildContext();
                ctx.setNsLambdaPolicy(NSLambdaPolicy.ERROR);
                JsonElement element = null;
                try {
                    element = ctx.doToJsonTree(fieldValue, f.getFieldAdapter(), refPathProducer.produce());
                } catch (NonSerializableLambdaException e) {
                    if (origCtx.getNsLambdaPolicy() == NSLambdaPolicy.ERROR) {
                        throw e;
                    } // else just discard the changes
                }

                if (element != null) {
                    if (wrapInObject && !hasExtraFieldsWritten) {
                        out.beginObject();
                    }
                    out.name(REF_FIELD_PREFIX + f.getField().getName());

                    Streams.write(element, out);
                    origCtx.mergeWithChildContext(ctx);
                    hasExtraFieldsWritten = true;
                }
            }
        }
        if (wrapInObject && hasExtraFieldsWritten) {
            out.endObject();
        }
    }

    public static void readAndSetReflectiveField(Object instance, String fname, Map<String, FieldInfo> reflectiveFields,
                                                 JsonReader in, ReadContext ctx, String valPathElement) throws IOException {
        FieldInfo fieldInfo = reflectiveFields.get(fname);
        if (fieldInfo == null) {
            throw new JsonSyntaxException("The " + instance.getClass() +
                    " does not have serializable reflective field '" + fname +"'");
        }
        // read value using the corresponding type adapter
        Object fieldValue = ctx.doRead(in, fieldInfo.getFieldAdapter(), valPathElement);

        // try set the field
        try {
            fieldInfo.getField().set(instance, fieldValue);
        } catch (IllegalAccessException e) {
            throw new JsonSyntaxException("Failed to set the reflective field value; instanceClass=" + instance.getClass() +
                    "; field=" + fieldInfo.getField() + "; value=" + fieldValue);
        }
    }

    /**
     * Given the actual object (and so the runtime type), resolves TypeAdapterRuntimeTypeWrapper to a non-dynamic
     * type adapter. This is used to determine the actual properties of the final resolved adapter (e.g. if isSimple)
     * @param typeAdapter the type adapter to resolve
     * @param value the actual object to be serialized with that adapter
     * @return returns the non-dynamic (resolved) type adapter which will be used for serializing the object
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeAdapter<T> resolve(TypeAdapter<T> typeAdapter, Object value) {
        if (typeAdapter instanceof DelegatingTypeAdapter && !(typeAdapter instanceof TypeInfoEmittingTypeAdapterWrapper)) {
            return resolve(((DelegatingTypeAdapter) typeAdapter).getDelegate(), value);
        } else if (typeAdapter instanceof TypeAdapterRuntimeTypeWrapper) {
            return resolve(((TypeAdapterRuntimeTypeWrapper)typeAdapter).resolve(value), value);
        } else {
            return typeAdapter;
        }
    }

    /**
     * Sets integer 'modCount' field, which is found in some collections and maps, to zero.
     */
    public static void clearModCount(Field modCountField, Object instance) {
      if (modCountField != null && instance != null) {
        try {
          modCountField.set(instance, 0);
        } catch (IllegalAccessException e) {
          throw new IllegalStateException("Failed to clear " + modCountField, e);
        }
      }
    }

}
