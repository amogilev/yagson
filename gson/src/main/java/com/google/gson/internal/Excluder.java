/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.gson.internal;

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;
import com.gilecode.yagson.refs.ReferencesPolicy;

import com.gilecode.yagson.strategy.*;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.gilecode.yagson.types.TypeUtils.safeClassForName;

/**
 * This class selects which fields and types to omit. It is configurable,
 * supporting version attributes {@link Since} and {@link Until}, modifiers,
 * synthetic fields, anonymous and local classes, inner classes, and fields with
 * the {@link Expose} annotation.
 *
 * <p>This class is a type adapter factory; types that are excluded will be
 * adapted to null. It may delegate to another type adapter if only one
 * direction is excluded.
 *
 * @author Joel Leitch
 * @author Jesse Wilson
 */
public final class Excluder implements TypeAdapterFactory, Cloneable {
  private static final double IGNORE_VERSIONS = -1.0d;
  public static final Excluder DEFAULT = new Excluder()
      .withExclusionStrategy(
              new ExcludeFieldsByDeclaringClasses(Reference.class, SoftReference.class, ReferenceQueue.class,
                      ThreadLocal.class),
              true, false)
      .withExclusionStrategy(
              new ExcludeFieldsInClassesByNames(Arrays.<Class<?>>asList(
                        Iterator.class,
                        safeClassForName(null, "java.util.SubList"),
                        safeClassForName(null, "java.util.HashMap$HashIterator"),
                        safeClassForName(null, "java.util.LinkedHashMap$LinkedHashIterator")
                      ),
                      "expectedModCount"),
              true, true)
      .withExclusionStrategy(
              new ExcludeFieldsInClassesByNames(safeClassForName(null,
                      "java.util.concurrent.CopyOnWriteArrayList$COWSubList"), "expectedArray"),
              true, true)
      .withExclusionStrategy(
              new ExcludeClassesAssignableTo(ClassLoader.class),
              true, false)
      .withTransientsExclusionStrategy(
              new ExcludeTransientFieldsInClassByNames(Map.class,
                      "keySet", "keySetView", "entrySet", "entrySetView", "values", "modCount"),
              true, true)
      .withTransientsExclusionStrategy(
              new ExcludeTransientFieldsInClassByNames(Collection.class,
                      "modCount"),
              true, true)
      .withTransientsExclusionStrategy(
              new ExcludeTransientFieldsInClassByNames(Throwable.class,
                      "backtrace"),
              true, true)
      .withTransientsExclusionStrategy(
              new ExcludeTransientFieldsInClassByNames(CopyOnWriteArrayList.class,
                      "array"),
              true, true)
      .withTransientsExclusionStrategy(
              new ExcludeTransientFieldsInDeclaringClassByNames(
                      Collections.newSetFromMap(Collections.<Object,Boolean>emptyMap()).getClass(),
                      "s"),
              true, true)
          ;

  private double version = IGNORE_VERSIONS;
  private int modifiers = Modifier.STATIC; // YaGson change: allow TRANSIENT by default
  private boolean serializeInnerClasses = true;
  private boolean requireExpose;
  private List<ExclusionStrategy> serializationStrategies = Collections.emptyList();
  private List<ExclusionStrategy> deserializationStrategies = Collections.emptyList();
  private List<TransientFieldExclusionStrategy> transientsSerializationStrategies = Collections.emptyList();
  private List<TransientFieldExclusionStrategy> transientsDeserializationStrategies = Collections.emptyList();
  private boolean serializeOuterReferences;
  private boolean serializeLocalAndAnonymousClasses;

  @Override protected Excluder clone() {
    try {
      return (Excluder) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }

  public Excluder withVersion(double ignoreVersionsAfter) {
    Excluder result = clone();
    result.version = ignoreVersionsAfter;
    return result;
  }

  public Excluder withModifiers(int... modifiers) {
    Excluder result = clone();
    result.modifiers = 0;
    for (int modifier : modifiers) {
      result.modifiers |= modifier;
    }
    return result;
  }

  public Excluder disableInnerClassSerialization() {
    Excluder result = clone();
    result.serializeInnerClasses = false;
    return result;
  }

  public Excluder excludeFieldsWithoutExposeAnnotation() {
    Excluder result = clone();
    result.requireExpose = true;
    return result;
  }

  public Excluder withExclusionStrategy(ExclusionStrategy exclusionStrategy,
      boolean serialization, boolean deserialization) {
    Excluder result = clone();
    if (serialization) {
      result.serializationStrategies = new ArrayList<ExclusionStrategy>(serializationStrategies);
      result.serializationStrategies.add(exclusionStrategy);
    }
    if (deserialization) {
      result.deserializationStrategies
          = new ArrayList<ExclusionStrategy>(deserializationStrategies);
      result.deserializationStrategies.add(exclusionStrategy);
    }
    return result;
  }

  public Excluder withTransientsExclusionStrategy(TransientFieldExclusionStrategy exclusionStrategyForTransientFields,
                                                  boolean serialization, boolean deserialization) {
    Excluder result = clone();
    if (serialization) {
      result.transientsSerializationStrategies
              = new ArrayList<TransientFieldExclusionStrategy>(transientsSerializationStrategies);
      result.transientsSerializationStrategies.add(exclusionStrategyForTransientFields);
    }
    if (deserialization) {
      result.transientsDeserializationStrategies
          = new ArrayList<TransientFieldExclusionStrategy>(transientsDeserializationStrategies);
      result.transientsDeserializationStrategies.add(exclusionStrategyForTransientFields);
    }
    return result;
  }

  public Excluder forReferencesPolicy(ReferencesPolicy referencesPolicy) {
    Excluder result = clone();
    result.serializeOuterReferences = referencesPolicy != ReferencesPolicy.DISABLED;

    // local and anonymous classes may have access to the final members of the enclosing class, so
    // require outer references support
    result.serializeLocalAndAnonymousClasses = result.serializeOuterReferences;
    return result;
  }

  public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
    Class<?> rawType = type.getRawType();
    boolean excludeClass = excludeClassChecks(rawType);

    final boolean skipSerialize = excludeClass || excludeClassInStrategy(rawType, true);
    final boolean skipDeserialize = excludeClass ||  excludeClassInStrategy(rawType, false);

    if (!skipSerialize && !skipDeserialize) {
      return null;
    }

    return new ExcluderTypeAdapter<T>(skipDeserialize, skipSerialize, gson, type);
  }

  public boolean excludeField(Field field, boolean serialize) {
    if ((modifiers & field.getModifiers()) != 0) {
      return true;
    }

    if (version != Excluder.IGNORE_VERSIONS
        && !isValidVersion(field.getAnnotation(Since.class), field.getAnnotation(Until.class))) {
      return true;
    }

    if (field.isSynthetic() && !serializeOuterReferences) {
      return true;
    }

    if (requireExpose) {
      Expose annotation = field.getAnnotation(Expose.class);
      if (annotation == null || (serialize ? !annotation.serialize() : !annotation.deserialize())) {
        return true;
      }
    }

    if (!serializeInnerClasses && isInnerClass(field.getType())) {
      return true;
    }

    if (!serializeLocalAndAnonymousClasses && isAnonymousOrLocal(field.getType())) {
      return true;
    }

    List<ExclusionStrategy> list = serialize ? serializationStrategies : deserializationStrategies;
    if (!list.isEmpty()) {
      FieldAttributes fieldAttributes = new FieldAttributes(field);
      for (ExclusionStrategy exclusionStrategy : list) {
        if (exclusionStrategy.shouldSkipField(fieldAttributes)) {
          return true;
        }
      }
    }

    if (Modifier.isTransient(field.getModifiers())) {
      List<TransientFieldExclusionStrategy> transientsExcludeList = serialize
              ? transientsSerializationStrategies : transientsDeserializationStrategies;
      if (!transientsExcludeList.isEmpty()) {
        for (TransientFieldExclusionStrategy strategy : transientsExcludeList) {
          if (strategy.shouldSkipField(field)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  private boolean excludeClassChecks(Class<?> clazz) {
      if (version != Excluder.IGNORE_VERSIONS && !isValidVersion(clazz.getAnnotation(Since.class), clazz.getAnnotation(Until.class))) {
          return true;
      }

      if (!serializeInnerClasses && isInnerClass(clazz)) {
          return true;
      }

    if (!serializeLocalAndAnonymousClasses && isAnonymousOrLocal(clazz)) {
      return true;
    }

      return false;
  }

  public boolean excludeClass(Class<?> clazz, boolean serialize) {
      return excludeClassChecks(clazz) ||
              excludeClassInStrategy(clazz, serialize);
  }

  private boolean excludeClassInStrategy(Class<?> clazz, boolean serialize) {
      List<ExclusionStrategy> list = serialize ? serializationStrategies : deserializationStrategies;
      for (ExclusionStrategy exclusionStrategy : list) {
          if (exclusionStrategy.shouldSkipClass(clazz)) {
              return true;
          }
      }
      return false;
  }

  private boolean isAnonymousOrLocal(Class<?> clazz) {
    return !Enum.class.isAssignableFrom(clazz)
        && (clazz.isAnonymousClass() || clazz.isLocalClass());
  }

  private boolean isInnerClass(Class<?> clazz) {
    return clazz.isMemberClass() && !isStatic(clazz);
  }

  private boolean isStatic(Class<?> clazz) {
    return (clazz.getModifiers() & Modifier.STATIC) != 0;
  }

  private boolean isValidVersion(Since since, Until until) {
    return isValidSince(since) && isValidUntil(until);
  }

  private boolean isValidSince(Since annotation) {
    if (annotation != null) {
      double annotationVersion = annotation.value();
      if (annotationVersion > version) {
        return false;
      }
    }
    return true;
  }

  private boolean isValidUntil(Until annotation) {
    if (annotation != null) {
      double annotationVersion = annotation.value();
      if (annotationVersion <= version) {
        return false;
      }
    }
    return true;
  }

  public class ExcluderTypeAdapter<T> extends TypeAdapter<T> {
    private final boolean skipDeserialize;
    private final boolean skipSerialize;
    private final Gson gson;
    private final TypeToken<T> type;
    /** The delegate is lazily created because it may not be needed, and creating it may fail. */
    private TypeAdapter<T> delegate;

    ExcluderTypeAdapter(boolean skipDeserialize, boolean skipSerialize, Gson gson, TypeToken<T> type) {
      this.skipDeserialize = skipDeserialize;
      this.skipSerialize = skipSerialize;
      this.gson = gson;
      this.type = type;
    }

    @Override public T read(JsonReader in, ReadContext ctx) throws IOException {
      if (skipDeserialize) {
        in.skipValue();
        return null;
      }
      return delegate().read(in, ctx);
    }

    @Override public void write(JsonWriter out, T value, WriteContext ctx) throws IOException {
      if (skipSerialize) {
        out.nullValue();
        return;
      }
      delegate().write(out, value, ctx);
    }

    private TypeAdapter<T> delegate() {
      TypeAdapter<T> d = delegate;
      return d != null
          ? d
          : (delegate = gson.getDelegateAdapter(Excluder.this, type));
    }

    public boolean isSkipSerialize() {
      return skipSerialize;
    }
  }
}
