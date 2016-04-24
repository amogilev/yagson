package com.google.gson;

import java.io.IOException;

import am.yagson.ReadContext;
import am.yagson.WriteContext;

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
  public T read(JsonReader in, ReadContext ctx) throws IOException {
    T value = read(in);
    ctx.refsContext().registerObject(null); // pass null as optimization - value is not available for referencing
    return value;
  }

  @Override
  public void write(JsonWriter out, T value, WriteContext ctx) throws IOException {
    write(out, value);
  }
}
