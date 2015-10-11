package com.google.gson;

import java.io.IOException;

import am.yagson.ReferencesReadContext;
import am.yagson.ReferencesWriteContext;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Type adapter for primitive or simple values, which are known to not contain any circular dependencies,
 * and so may be safely ignored by the reference context.
 * 
 * @author Andrey Mogilev
 */
public abstract class SimpleTypeAdapter<T> extends TypeAdapter<T> {

  abstract public void write(JsonWriter out, T value) throws IOException;
  abstract public T read(JsonReader in) throws IOException;
  
  @Override
  public T read(JsonReader in, ReferencesReadContext rctx) throws IOException {
    T value = read(in);
    rctx.registerObject(value, true);
    return value;
  }

  @Override
  public void write(JsonWriter out, T value, ReferencesWriteContext rctx) throws IOException {
    write(out, value);
  }
  
  @Override
  public boolean hasSimpleJsonFor(T value) {
    return true;
  }
}
