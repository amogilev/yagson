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

import am.yagson.ReadContext;
import am.yagson.WriteContext;
import am.yagson.refs.*;

import am.yagson.types.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

import static am.yagson.refs.References.REF_FIELD_PREFIX;
import static am.yagson.types.TypeUtils.classOf;

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
    boolean isSet = Set.class.isAssignableFrom(collType);
    try {
      boolean isAddMethodMissing = AbstractCollection.class.isAssignableFrom(collType) &&
              !TypeUtils.isOverridden(collType, AbstractCollection.class.getDeclaredMethod("add", Object.class));
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
                (isAddMethodMissing && !collType.getName().contains("EmptySet"));
      } else {
        return isDelegate || isAddMethodMissing;
      }
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }


  private static final class Adapter<E> extends TypeAdvisableComplexTypeAdapter<Collection<E>> {
    private final TypeAdapter<E> elementTypeAdapter;
    private final ObjectConstructor<? extends Collection<E>> constructor;
    private final ConstructorConstructor constructorConstructor;
    private final Gson gson;
    private final Type formalCollType; // declared collection type, e.g. SortedSet<String>
    private Collection<E> defaultCollInstance; // the default Collection instance, as returned by the constructor
    private final Class<? extends Collection> defaultCollClass; // raw class of the default collection instance (defined by constructor)

    // TODO: may be not required, check after completing all tests
//    private final Map<String, FieldInfo> reflectiveFields; // reflective (extra) fields for default collection type

    private final FieldInfo defaultBackingMapFieldInfo;
    private final Field defaultBackingMapComparatorField;

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
        this.defaultBackingMapFieldInfo = findBackingMap(defaultCollClass, ExistingObjectProvider.of(defaultCollInstance));
        this.defaultBackingMapComparatorField = findComparatorField(defaultBackingMapFieldInfo);
      } else {
        this.defaultCollClass = null;
        this.defaultBackingMapFieldInfo = null;
        this.defaultBackingMapComparatorField = null;
      }

//      this.reflectiveFields = buildReflectiveFieldsInfo(defaultCollClass,
//              new ExistingObjectProvider(defaultCollInstance));
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
      f.setAccessible(true);
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

      Map<String, FieldInfo> mapFields = AdapterUtils.buildReflectiveFieldsInfo(gson, collClass, formalCollType,
              collProvider, classOf(Map.class), classOf(Properties.class));

      if (mapFields.size() != 1) {
        // if several Map fields are found, we cannot determine for sure which one is backing map
        return null;
      }

      return mapFields.values().iterator().next();
    }

//    private Map<String, FieldInfo> buildReflectiveFieldsInfo(Class<? extends Collection> containerClass,
//                                                             ObjectProvider<? extends Collection> objectProvider) {
//      return AdapterUtils.buildReflectiveFieldsInfo(gson, containerClass, formalCollType,
//              objectProvider, classOf(Comparator.class), null);
//    }

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

      boolean nextIsBackingMapValue = false;

      FieldInfo localBackingMapInfo = null;

      for (int i = 0; in.hasNext(); i++) {
        if (nextIsBackingMapValue) {
          // read the backing map and save into the instance
          nextIsBackingMapValue = false;
          String fieldName = localBackingMapInfo.getField().getName();
          AdapterUtils.readAndSetReflectiveField(instance, fieldName,
                  Collections.singletonMap(fieldName, localBackingMapInfo),
                  in, ctx, fieldName);
          continue;
        }
        if (i == 0 && in.peek() == JsonToken.STRING && gson.getTypeInfoPolicy().isEnabled()) {
          String fieldRef = in.nextString();
          if (fieldRef.startsWith(REF_FIELD_PREFIX) && fieldRef.endsWith(":")) {
            String fieldName = fieldRef.substring(REF_FIELD_PREFIX.length(), fieldRef.length() - 1);
            localBackingMapInfo = defaultCollClass == instance.getClass() ? defaultBackingMapFieldInfo :
                    this.findBackingMap(instance.getClass(), new ExistingObjectProvider(instance));
            if (localBackingMapInfo == null || !fieldName.equals(localBackingMapInfo.getField().getName())) {
              throw new JsonSyntaxException("The only expected field reference in this collection is " +
                      REF_FIELD_PREFIX + fieldName + ":");
            }

            nextIsBackingMapValue = true;
            continue;
          } else {
            // general string found, not an extra field
            JsonReaderInternalAccess.INSTANCE.returnStringToBuffer(in, fieldRef);
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
            }
          });
          // null will be added to the list now, and it will be replaced to an actual object in future
        }

        instance.add(elementInstance);
      }
      in.endArray();

      return instance;
    }

    private boolean isBackingMapDefault(FieldInfo backingMapInfo, Class<? extends Collection> actualCollClass, Object actualBackingMap) {
      Object defaultBackingMap = backingMapInfo.getDefaultValue();

      if (defaultBackingMap == null || actualBackingMap == null) {
        return defaultBackingMap == actualBackingMap;
      }

      if (defaultBackingMap.getClass() != actualBackingMap.getClass()) {
        return false;
      }

      // backing map class is default, additionally check if comparator is default
      Field comparatorField = actualCollClass == defaultCollClass ?
              defaultBackingMapComparatorField : findComparatorField(backingMapInfo);

      if (comparatorField == null) {
        return true;
      }

      Object defaultComparator = getField(defaultBackingMap, comparatorField);
      Object actualComparator = getField(actualBackingMap, comparatorField);

      return defaultComparator == null ? actualComparator == null : defaultComparator.equals(actualComparator);
    }

    private int writeEmptyBackingMapIfNonDefault(Collection<E> actualCollInstance, JsonWriter out, WriteContext ctx) throws IOException {
      if (!gson.getTypeInfoPolicy().isEnabled()) {
        return 0;
      }

      Class<? extends Collection> actualCollClass = actualCollInstance.getClass();

      FieldInfo backingMapInfo;
      if (actualCollClass != defaultCollClass) {
        backingMapInfo = findBackingMap(actualCollClass, ConstructingObjectProvider.defaultOf(actualCollInstance, constructorConstructor));
      } else {
        backingMapInfo = defaultBackingMapFieldInfo;
      }
      if (backingMapInfo == null) {
        return 0;
      }

      Field backingMapField = backingMapInfo.getField();
      Object actualBackingMap = getField(actualCollInstance, backingMapField);
      if (isBackingMapDefault(backingMapInfo, actualCollClass, actualBackingMap)) {
        return 0;
      }

      out.value(REF_FIELD_PREFIX + backingMapField.getName() + ":");
      ctx.setSkipNextMapEntries(true);
      ctx.doWrite(actualBackingMap, backingMapInfo.getFieldAdapter(), "1", out);
      ctx.setSkipNextMapEntries(false);

      return 2; // next element index, after written name and value elements
    }

//    private void writeExtraFields(JsonWriter out, Collection<E> coll, ReferencesWriteContext rctx, int startIdx)
//            throws IOException {
//      Map<String, FieldInfo> localReflectiveFields = defaultCollClass == coll.getClass() ? reflectiveFields :
//              buildReflectiveFieldsInfo(coll.getClass(), new ConstructingObjectProvider(
//                      constructorConstructor.get(TypeToken.get(coll.getClass()))));
//      final AtomicInteger mapEntryIdx = new AtomicInteger(startIdx);
//      AdapterUtils.writeReflectiveFields(coll, localReflectiveFields, out, rctx, new PathElementProducer() {
//        public String produce() {
//          int i = mapEntryIdx.getAndIncrement();
//          return Integer.toString(i);
//        }
//      }, true);
//    }

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

      out.beginArray();

      int i = writeEmptyBackingMapIfNonDefault(collection, out, ctx);

      for (E element : collection) {
        ctx.doWrite(element, elementTypeAdapter, Integer.toString(i), out);
        i++;
      }
//      writeExtraFields(out, collection, rctx, i);
      out.endArray();
    }
  }
}
