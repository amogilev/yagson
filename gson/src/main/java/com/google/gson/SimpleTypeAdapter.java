package com.google.gson;

import java.io.IOException;

import am.yagson.ReferencesContext;

import com.google.gson.stream.JsonWriter;

/**
 * Type adapter for primitive or simple values, which are known to not contain any circular dependencies,
 * and so may be safely ignored by the reference context.
 * 
 * @author Andrey Mogilev
 */
public abstract class SimpleTypeAdapter<T> extends TypeAdapter<T> {

  abstract public void write(JsonWriter out, T value) throws IOException;
  
  @Override
  public void write(JsonWriter out, T value, ReferencesContext refsContext) throws IOException {
    write(out, value);
  }
}
