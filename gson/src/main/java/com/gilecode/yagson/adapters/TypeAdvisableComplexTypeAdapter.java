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

import java.io.IOException;

import com.gilecode.yagson.ReadContext;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * Helper abstract class for non-simple type adapters, which provides references read support.
 *
 * @author Andrey Mogilev
 */
public abstract class TypeAdvisableComplexTypeAdapter<T> extends TypeAdapter<T> {

  public T read(JsonReader in, ReadContext ctx) throws IOException {
    JsonToken nextToken = in.peek();

    if (nextToken == JsonToken.NULL) {
      in.nextNull();
      return null;
    } else if (nextToken == JsonToken.STRING) {
      // for complex type adapters, each string is a reference, no isReferenceString() match required
      String reference = in.nextString();
      if (!reference.startsWith("@")) {
        throw new JsonSyntaxException("Expected JSON Array, Object or YaGson reference, but got string literal: '" +
                reference + "'");
      }
      T referenced = ctx.refsContext().getReferencedObject(reference);
      return referenced;
    }

    return readOptionallyAdvisedInstance(in, ctx);
  }

  /**
   * Actually reads an object, given that no references are available. The implementations of this method MUST support
   * type advice wrappers, and MUST distinguish them from other JSON Objects.
   * <p/>
   * A created instance MUST be registered within the provided read context, before any child element is read.
   *
   * @param in the reader
   * @param ctx the read context
   *
   * @return the created instance, filled with data
   */
  protected abstract T readOptionallyAdvisedInstance(JsonReader in, ReadContext ctx) throws IOException;
}
