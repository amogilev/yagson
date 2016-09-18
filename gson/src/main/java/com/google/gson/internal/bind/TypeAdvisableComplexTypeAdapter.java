package com.google.gson.internal.bind;

import java.io.IOException;

import com.gilecode.yagson.ReadContext;

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
      T referenced = ctx.refsContext().getReferencedObject(reference);
      return referenced;
    }

    return readOptionallyAdvisedInstance(in, ctx);
  }

  /**
   * Actually reads an object, given that no references are available. The implementations of this method MUST support
   * type advice wrappers, and MUST distinguish them from other JSON Objects.
   * <p/>
   * A created instance MUST be registered within the provided read context.
   *
   * @param in the reader
   * @param ctx the read context
   *
   * @return the created instance, filled with data
   */
  protected abstract T readOptionallyAdvisedInstance(JsonReader in, ReadContext ctx) throws IOException;
}
