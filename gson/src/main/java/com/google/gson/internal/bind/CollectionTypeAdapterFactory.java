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

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;
import com.gilecode.yagson.adapters.AdapterUtils;
import com.gilecode.yagson.adapters.TypeAdvisableComplexTypeAdapter;
import com.gilecode.yagson.refs.*;
import com.gilecode.yagson.types.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.*;
import com.google.gson.internal.reflect.ReflectionAccessor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static com.gilecode.yagson.refs.References.REF_FIELD_PREFIX;
import static com.gilecode.yagson.types.TypeUtils.classOf;

/**
 * Adapt a homogeneous collection of objects.
 */
public final class CollectionTypeAdapterFactory implements TypeAdapterFactory {
  private final ConstructorConstructor constructorConstructor;
  private final static ReflectionAccessor accessor = ReflectionAccessor.getInstance();

  public CollectionTypeAdapterFactory(ConstructorConstructor constructorConstructor) {
    this.constructorConstructor = constructorConstructor;
  }

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
    Type type = typeToken.getType();

    Class<? super T> rawType = typeToken.getRawType();
    if (!Collection.class.isAssignableFrom(rawType)) {
      return null;
    }

    if (requireReflectiveAdapter(gson, rawType)) {
      // serialize as general class
      return null;
    }

    Type elementType = $Gson$Types.getCollectionElementType(type, rawType);
    TypeAdapter<?> elementTypeAdapter = gson.getAdapter(TypeToken.get(elementType));
    ObjectConstructor<T> constructor = constructorConstructor.get(typeToken);

    @SuppressWarnings({"unchecked", "rawtypes"}) // create() doesn't define a type parameter
    TypeAdapter<T> result = new Adapter(gson, type, elementType, elementTypeAdapter, constructor, constructorConstructor);
    return result;
  }

  /**
   * Determines whether the specified collection type shall be processed by a {@link ReflectiveTypeAdapterFactory}
   * rather than as general collection. This is required to support special and delegate collections like unmodifiableSet
   * when the type info is enabled.
   * <p/>
   * The current rules: a set shall be processed by reflective type adapter if it contains at least one
   * non-transient Collection field.
   * <p/>
   * Note that even if the type does not require using reflective adapter, the non-default backing map or comparator
   * in the set instance still may require it.
   */
  private static boolean requireReflectiveAdapter(Gson gson, Class<?> collType) {
    if (!gson.getTypeInfoPolicy().isEnabled() || !TypeUtils.isGeneralNonAbstractClass(collType)) {
      return false;
    }
    boolean isEnclosed = collType.getEnclosingClass() != null;
    boolean isSet = Set.class.isAssignableFrom(collType);
    boolean isList = List.class.isAssignableFrom(collType);
    try {
      boolean isAddMethodMissing;
      if (isList) {
        isAddMethodMissing = AbstractList.class.isAssignableFrom(collType) &&
                !TypeUtils.isOverridden(collType, AbstractList.class.getDeclaredMethod("add", int.class, Object.class));
      } else {
        isAddMethodMissing = AbstractCollection.class.isAssignableFrom(collType) &&
                !TypeUtils.isOverridden(collType, AbstractCollection.class.getDeclaredMethod("add", Object.class));
      }
      // consider delegate collection if contains another collection and has no public no-arg constructors
      boolean isDelegate = false;
      if (TypeUtils.containsField(collType, false, classOf(Collection.class), null)) {
        try {
          collType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
          isDelegate = true;
        }
      }

      if (isSet) {
        return isDelegate ||
                collType.getName().equals("java.util.Collections$SetFromMap") ||
                ((isAddMethodMissing || isEnclosed) && !collType.getName().contains("EmptySet"));
      } else if (isList) {
        return isDelegate ||
                ((isAddMethodMissing || isEnclosed) && !collType.getName().contains("EmptyList"));
      } else {
        return isDelegate || isAddMethodMissing || isEnclosed
                // || BeanContext.class.isAssignableFrom(collType) -- replaced to startWith to support Android
                || collType.getName().startsWith("java.beans.beancontext.BeanContext")
                || BlockingQueue.class.isAssignableFrom(collType);
      }
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }


  private static final class Adapter<E> extends TypeAdvisableComplexTypeAdapter<Collection<E>> {
    private static class ReflectiveFieldsInfo {
      // backing map field found in the default collection class, with default instance information
      private final FieldInfo backingMapFieldInfo;
      // the comparator field found in the default implementation of the backing map
      private final Field defaultBackingMapComparatorField;
      // reflective (extra) fields for default collection type, includes backingMapFieldInfo if exists
      private final Map<String, FieldInfo> reflectiveFields;

      private ReflectiveFieldsInfo(FieldInfo backingMapFieldInfo, Field defaultBackingMapComparatorField,
                                   Map<String, FieldInfo> reflectiveFields) {
        this.backingMapFieldInfo = backingMapFieldInfo;
        this.defaultBackingMapComparatorField = defaultBackingMapComparatorField;
        this.reflectiveFields = reflectiveFields;
      }
    }

    private final TypeAdapter<E> elementTypeAdapter;
    private final ObjectConstructor<? extends Collection<E>> constructor;
    private final ConstructorConstructor constructorConstructor;
    private final Gson gson;
    private final Type formalCollType; // declared collection type, e.g. SortedSet<String>
    private Collection<E> defaultCollInstance; // the default Collection instance, as returned by the constructor
    private final Class<? extends Collection> defaultCollClass; // raw class of the default collection instance (defined by constructor)
    private final Field modCountField; // modCount field found in the default collection class, if any

    // information about the fields which may need to be written/read reflectively, in addition to the collection elements
    private final ReflectiveFieldsInfo reflectiveFieldsInfo;

    Adapter(Gson gson, Type formalCollType, Type elementType,
                   TypeAdapter<E> elementTypeAdapter,
                   ObjectConstructor<? extends Collection<E>> constructor,
                   ConstructorConstructor constructorConstructor) {
      this.gson = gson;
      this.formalCollType = formalCollType;
      this.elementTypeAdapter =
          new TypeAdapterRuntimeTypeWrapper<E>(gson, elementTypeAdapter, elementType,
                  TypeUtils.getEmitTypeInfoRule(gson, false));
      this.constructor = constructor;
      this.constructorConstructor = constructorConstructor;

      if (gson.getTypeInfoPolicy().isEnabled()) {
        try {
          this.defaultCollInstance = constructor.construct();
        } catch (Exception e) {
          // the default instance is inconstructible (e.g. abstract with no default impl.)
          // this is fine, as the type/adapter may be refined in future
        }
      }

      if (defaultCollInstance != null) {
        this.defaultCollClass = defaultCollInstance.getClass();
        this.reflectiveFieldsInfo = buildReflectiveFieldsInfo(defaultCollClass,
                ExistingObjectProvider.of(defaultCollInstance));
      } else {
        this.defaultCollClass = null;
        this.reflectiveFieldsInfo = null;
      }
      this.modCountField = getModCountField(defaultCollClass);
    }

    private Field getModCountField(Class<?> collClass) {
      return TypeUtils.findField(collClass, "modCount");
    }

    private Field findComparatorField(FieldInfo backingMapFieldInfo) {
      if (backingMapFieldInfo == null || backingMapFieldInfo.getDefaultValue() == null) {
        return null;
      }
      Object map = backingMapFieldInfo.getDefaultValue();
      List<Field> compFields = TypeUtils.findFields(map.getClass(), true, classOf(Comparator.class), null);
      if (compFields.isEmpty()) {
        return null;
      }
      Field f = compFields.get(0);
      accessor.makeAccessible(f);
      return f;
    }

    private Object getField(Object instance, Field f) {
      try {
        return f == null ? null : f.get(instance);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("Reflective read failed", e);
      }
    }

    private FieldInfo findBackingMap(Class<? extends Collection> collClass, ObjectProvider<? extends Collection> collProvider) {
      if (!Set.class.isAssignableFrom(collClass)) {
        // only for sets
        return null;
      }

      Map<String, FieldInfo> mapFields = AdapterUtils.buildReflectiveFieldsInfo(gson, collClass,
              collProvider, classOf(Map.class), classOf(Properties.class));

      if (mapFields.size() != 1) {
        // if several Map fields are found, we cannot determine for sure which one is backing map
        return null;
      }

      return mapFields.values().iterator().next();
    }

    private ReflectiveFieldsInfo buildReflectiveFieldsInfo(Class<? extends Collection> containerClass,
                                                           ObjectProvider<? extends Collection> objectProvider) {
      FieldInfo backingMapFieldInfo = findBackingMap(defaultCollClass, objectProvider);
      Field defaultBackingMapComparatorField = findComparatorField(backingMapFieldInfo);
      Map<String, FieldInfo> reflectiveFields = AdapterUtils.buildReflectiveFieldsInfo(gson, containerClass,
              objectProvider, classOf(Comparator.class), null);
      if (backingMapFieldInfo != null) {
        reflectiveFields.put(backingMapFieldInfo.getField().getName(), backingMapFieldInfo);
      }
      return new ReflectiveFieldsInfo(backingMapFieldInfo, defaultBackingMapComparatorField, reflectiveFields);
    }

    @Override
    protected Collection<E> readOptionallyAdvisedInstance(JsonReader in, ReadContext ctx)
        throws IOException {

      if (in.peek() == JsonToken.BEGIN_OBJECT) {
        // must be a type advice
        return TypeUtils.readTypeAdvisedValue(gson, in, formalCollType, ctx);
      }

      final Collection<E> instance = constructor.construct();
      ctx.registerObject(instance, false);

      in.beginArray();

      // whether reflective field is possible at next position
      boolean awaitReflectiveFields = gson.getTypeInfoPolicy().isEnabled();
      // if non-null, the next value in collection is for this reflective field
      String nextReflectiveField = null;
      // lazy reflective fields information for the actual collection class
      ReflectiveFieldsInfo localReflectiveFieldsInfo = null;

      // modCount field for the actual collection class; created only if collection is not empty
      final Field localModCountField = !in.hasNext() ? null :
              (defaultCollClass == instance.getClass() ? modCountField : getModCountField(instance.getClass()));

      for (int i = 0; in.hasNext(); i++) {
        if (nextReflectiveField != null) {
          // read the backing map and save into the instance
          AdapterUtils.readAndSetReflectiveField(instance, nextReflectiveField,
                  localReflectiveFieldsInfo.reflectiveFields,
                  in, ctx, Integer.toString(i));
          nextReflectiveField = null;
          continue;
        }
        if (awaitReflectiveFields && in.peek() == JsonToken.STRING) {
          String fieldRef = in.nextString();
          if (fieldRef.startsWith(REF_FIELD_PREFIX) && fieldRef.endsWith(":")) {
            if (localReflectiveFieldsInfo == null) {
              localReflectiveFieldsInfo = defaultCollClass == instance.getClass() ? reflectiveFieldsInfo :
                      buildReflectiveFieldsInfo(instance.getClass(), ExistingObjectProvider.of(instance));
            }
            nextReflectiveField = fieldRef.substring(REF_FIELD_PREFIX.length(), fieldRef.length() - 1);
            continue;
          } else {
            // general string found, not an extra field
            JsonReaderInternalAccess.INSTANCE.returnStringToBuffer(in, fieldRef);
            awaitReflectiveFields = false;
          }
        }

        E elementInstance = ctx.doRead(in, elementTypeAdapter, Integer.toString(i));
        ReferencePlaceholder<E> elementPlaceholder;
        if (elementInstance == null && ((elementPlaceholder = ctx.refsContext().consumeLastPlaceholderIfAny()) != null)) {
          final int fi = i;
          elementPlaceholder.registerUse(new PlaceholderUse<E>() {
            public void applyActualObject(E actualObject) {
              if (instance instanceof List) {
                ((List<E>)instance).set(fi, actualObject);
              } else  {
                // no set() method available, have to re-build the collection to ensure the right order
                List<E> l = new ArrayList<E>(instance);
                instance.clear();
                instance.addAll(l.subList(0, fi));
                instance.add(actualObject);
                instance.addAll(l.subList(fi + 1, l.size()));
              }
              AdapterUtils.clearModCount(localModCountField, instance);
            }
          });
          // null will be added to the list now, and it will be replaced to an actual object in future
        }

        instance.add(elementInstance);
      }
      in.endArray();
      if (localModCountField != null) {
        AdapterUtils.clearModCount(localModCountField, instance);
      }

      return instance;
    }

    private boolean isBackingMapDefault(FieldInfo backingMapInfo, Field comparatorField, Object actualBackingMap) {
      Object defaultBackingMap = backingMapInfo.getDefaultValue();

      if (defaultBackingMap == null || actualBackingMap == null) {
        return defaultBackingMap == actualBackingMap;
      }

      if (defaultBackingMap.getClass() != actualBackingMap.getClass()) {
        return false;
      }

      // backing map class is default, additionally check if comparator is default
      if (comparatorField == null) {
        return true;
      }

      Object defaultComparator = getField(defaultBackingMap, comparatorField);
      Object actualComparator = getField(actualBackingMap, comparatorField);

      return defaultComparator == null ? actualComparator == null : defaultComparator.equals(actualComparator);
    }

    private int writeEmptyBackingMapIfNonDefault(ReflectiveFieldsInfo localReflectiveFieldsInfo, Collection<E> actualCollInstance,
                                                 JsonWriter out, WriteContext ctx, int idx) throws IOException {
      FieldInfo backingMapInfo = localReflectiveFieldsInfo.backingMapFieldInfo;
      if (backingMapInfo == null) {
        return idx;
      }

      Field backingMapField = backingMapInfo.getField();
      Object actualBackingMap = getField(actualCollInstance, backingMapField);
      if (isBackingMapDefault(backingMapInfo, localReflectiveFieldsInfo.defaultBackingMapComparatorField, actualBackingMap)) {
        return idx;
      }

      // do actual write only if does not contain non-serializable lambdas
      WriteContext origCtx = ctx;
      ctx = ctx.makeChildContext();
      ctx.setSkipNextMapEntries(true);
      ctx.setNsLambdaPolicy(NSLambdaPolicy.ERROR);

      JsonElement element;
      try {
        element = ctx.doToJsonTree(actualBackingMap, backingMapInfo.getFieldAdapter(), Integer.toString(idx + 1));
      } catch (NonSerializableLambdaException e) {
        if (origCtx.getNsLambdaPolicy() == NSLambdaPolicy.ERROR) {
          throw e;
        } // else just discard the changes
        return idx; // nothing is written
      }
      out.value(REF_FIELD_PREFIX + backingMapField.getName() + ":");
      Streams.write(element, out);
      origCtx.mergeWithChildContext(ctx);
      return idx + 2; // next element index, after written name and value elements
    }

    private int writeExtraFields(Collection<E> coll, JsonWriter out, WriteContext ctx)
            throws IOException {
      if (!gson.getTypeInfoPolicy().isEnabled()) {
        return 0;
      }

      ReflectiveFieldsInfo localReflectiveFieldsInfo = defaultCollClass == coll.getClass() ? reflectiveFieldsInfo :
              buildReflectiveFieldsInfo(coll.getClass(),
                      ConstructingObjectProvider.defaultOf(coll, constructorConstructor));

      int idx = 0;
      for (FieldInfo f : localReflectiveFieldsInfo.reflectiveFields.values()) {
        Object fieldValue;
        try {
          fieldValue = f.getField().get(coll);
        } catch (IllegalAccessException e) {
          throw new IOException("Failed to read field", e);
        }
        if (f == localReflectiveFieldsInfo.backingMapFieldInfo) {
          // special handling for backing maps - write an empty clone instead
          idx = writeEmptyBackingMapIfNonDefault(localReflectiveFieldsInfo, coll, out, ctx, idx);
        } else {
          // skip default values
          if (fieldValue != f.getDefaultValue() && (fieldValue == null || !fieldValue.equals(f.getDefaultValue()))) {

            // do actual write only if does not contain non-serializable lambdas
            WriteContext origCtx = ctx;
            ctx = ctx.makeChildContext();
            ctx.setNsLambdaPolicy(NSLambdaPolicy.ERROR);
            try {
              JsonElement element = ctx.doToJsonTree(fieldValue, f.getFieldAdapter(), Integer.toString(idx + 1));
              out.value(REF_FIELD_PREFIX + f.getField().getName() + ":");
              Streams.write(element, out);
              origCtx.mergeWithChildContext(ctx);
              idx += 2; // spans two elements
            } catch (NonSerializableLambdaException e) {
              if (origCtx.getNsLambdaPolicy() == NSLambdaPolicy.ERROR) {
                throw e;
              } // else just discard the changes
            }
          }
        }
      }

      return idx;
    }

    @Override
    public void write(JsonWriter out, Collection<E> collection, WriteContext ctx) throws IOException {
      if (collection == null) {
        out.nullValue();
        return;
      }

      if (defaultCollClass != collection.getClass() && requireReflectiveAdapter(gson, collection.getClass())) {
        // although the condition was checked in the factory for the formal type, it shall be
        // re-checked for the actual class if it differs

        AdapterUtils.writeByReflectiveAdapter(out, collection, ctx, gson, formalCollType);
        return;
      }

      // NOTE: serializing "as collection" requires call of custom class-provided methods: Collection.iterator()
      //       and the methods of that iterator, which is not perfectly safe. There are examples where iterator()
      //       throws UnsupportedOperationException, and potentially hasNext()/next() may throw exceptions too.
      //       Currently only iterator() is checked before decision to use "as collection" vs. "as object"
      //       serialization, but if more examples will be found, it would be more safe to copy everything to a
      //       temporal ArrayList and use "as object" if failed.
      Iterator<E> it;
      try {
        it = collection.iterator();
      } catch (Exception e) {
        AdapterUtils.writeByReflectiveAdapter(out, collection, ctx, gson, formalCollType);
        return;
      }

      out.beginArray();
      int i = writeExtraFields(collection, out, ctx);

      while (it.hasNext()) {
        E element = it.next();
        ctx.doWrite(element, elementTypeAdapter, Integer.toString(i), out);
        i++;
      }

      out.endArray();
    }
  }
}
