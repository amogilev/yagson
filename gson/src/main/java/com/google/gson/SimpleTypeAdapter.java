package com.google.gson;

import java.io.IOException;

import am.yagson.ReadContext;
import am.yagson.WriteContext;

import am.yagson.types.TypeUtils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Type adapter for primitive or simple values, which are known to not contain any child objects inside, and
 * the generated JSON representation does not contain JSON Objects or Arrays ('{...}' and '[...]'}.
 * <p/>
 * In particular, it means that the corresponding objects cannot contain circular dependencies.
 * <p/>
 * For convenience, in all current references policies, no references are created for such primitove objects.
 * Otherwise, in references policies like 'all duplicates', we could have many annoying references for values like empty
 * string, zero etc.
 *
 * @author Andrey Mogilev
 */
public abstract class SimpleTypeAdapter<T> extends TypeAdapter<T> {

  abstract public void write(JsonWriter out, T value) throws IOException;
  abstract public T read(JsonReader in) throws IOException;
  
  @Override
  public T read(JsonReader in, ReadContext ctx) throws IOException {
    T value = read(in);
    ctx.registerObject(value, true);
    return value;
  }

  @Override
  public void write(JsonWriter out, T value, WriteContext ctx) throws IOException {
    write(out, value);
  }
}
