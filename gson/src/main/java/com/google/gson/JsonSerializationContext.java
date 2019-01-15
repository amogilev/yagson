/*
 * Copyright (C) 2008 Google Inc.
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

package com.google.gson;

import com.gilecode.yagson.WriteContext;

import java.lang.reflect.Type;

/**
 * Context for serialization that is passed to a custom serializer during invocation of its
 * {@link JsonSerializer#serialize(Object, Type, JsonSerializationContext)} method.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public interface JsonSerializationContext {

  /**
   * Invokes default serialization on the specified object.
   *
   * @param src the object that needs to be serialized.
   * @param pathElement the path element which defines path from the parent object to src, e.g. '@root' for the root object,
   *                    the field name for fields, array index for array elements etc.
   * @return a tree of {@link JsonElement}s corresponding to the serialized form of {@code src}.
   */
  public JsonElement serialize(Object src, String pathElement);

  /**
   * Invokes default serialization on the specified object passing the specific type information.
   * It should never be invoked on the element received as a parameter of the
   * {@link JsonSerializer#serialize(Object, Type, JsonSerializationContext)} method. Doing
   * so will result in an infinite loop since Gson will in-turn call the custom serializer again.
   *
   * @param src the object that needs to be serialized.
   * @param typeOfSrc the actual genericized type of src object.
   * @param pathElement the path element which defines path from the parent object to src, e.g. '@root' for the root object,
   *                    the field name for fields, array index for array elements etc.
   * @return a tree of {@link JsonElement}s corresponding to the serialized form of {@code src}.
   */
  public JsonElement serialize(Object src, String pathElement, Type typeOfSrc);

  /**
   * Serialize invoked as a delegate, with the path and the object already processed by the context.
   * Also can be used for serialization of the root objects
   */
  public JsonElement delegatedOrRootSerialize(Object src);

  /**
   * Serialize invoked as a delegate, with the path and the object already processed by the context.
   * Also can be used for serialization of the root objects
   */
  public JsonElement delegatedOrRootSerialize(Object src, Type typeOfSrc);

  /**
   * Returns the write context used in YaGson
   */
  WriteContext getWriteContext();


}
