package com.google.gson.internal.bind;

import java.io.IOException;
import java.lang.reflect.Array;

import am.yagson.ReferencesReadContext;
import am.yagson.ReferencesWriteContext;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public abstract class TypeAdvisableComplexTypeAdapter<T> extends TypeAdapter<T> {
  
  class Delegate extends TypeAdapter<T> {
    
    private final Class<T> advisedType;
    private final Gson context;
    
    public Delegate(Class<T> advisedType, Gson context) {
      this.advisedType = advisedType;
      this.context = context;
    }

    @Override
    public void write(JsonWriter out, T value, ReferencesWriteContext rctx) throws IOException {
      TypeAdvisableComplexTypeAdapter.this.write(out, value, rctx);
    }

    @Override
    public T read(JsonReader in, ReferencesReadContext rctx) throws IOException {
      JsonToken nextToken = in.peek();
      
      if (nextToken == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      
      T referenced = rctx.checkReferenceUse(in);
      if (referenced != null) {
        return referenced;
      }
      
      T instance;
      if (advisedType.isArray()) {
        Class<?> advisedComponentType = advisedType.getComponentType();
        instance = (T) Array.newInstance(advisedComponentType, 0);
      } else {
        instance = context.getConstructorConstructor().get(TypeToken.get(advisedType)).construct();
      }

      return readOptionallyAdvisedInstance(instance, in, rctx);
    }
  }

  public T read(JsonReader in, ReferencesReadContext rctx) throws IOException {
    JsonToken nextToken = in.peek();
    
    if (nextToken == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    
    T referenced = rctx.checkReferenceUse(in);
    if (referenced != null) {
      return referenced;
    }

    return readOptionallyAdvisedInstance(null, in, rctx);
  }

  /**
   * Read an object, optionally using the advised instance. If none
   * advised, or if another type advice exists, a new instance may be created.
   * A chosen instance MUST be registered with rctx.
   *
   * @param advisedInstance the advised instance, or {@code null}
   * @param in the reader
   * @param rctx the references context
   *
   * @return the chosen instance, filled with data
   */
  protected abstract T readOptionallyAdvisedInstance(T advisedInstance, JsonReader in,
                                                     ReferencesReadContext rctx) throws IOException;
    
}
