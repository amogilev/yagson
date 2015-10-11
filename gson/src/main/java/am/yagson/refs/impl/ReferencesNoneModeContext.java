package am.yagson.refs.impl;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import am.yagson.ReferencesPolicy;
import am.yagson.ReferencesReadContext;
import am.yagson.ReferencesWriteContext;

public class ReferencesNoneModeContext implements ReferencesReadContext, ReferencesWriteContext {

  public static ReferencesNoneModeContext instance = new ReferencesNoneModeContext(); 

  public <T> JsonElement doToJsonTree(T value, TypeAdapter<T> valueTypeAdapter,
      String pathElement) {
    return valueTypeAdapter.toJsonTree(value, this);
  }

  public <T> void doWrite(T value, TypeAdapter<T> valueTypeAdapter,
      String pathElement, JsonWriter out) throws IOException {
    valueTypeAdapter.write(out, value, this);
  }

  public void registerObject(Object value, boolean isSimple) {
    // do nothing
  }

  public <T> T doRead(JsonReader reader, TypeAdapter<T> typeAdapter,
      String pathElement) throws IOException {
    return typeAdapter.read(reader, this);
  }

  public <T> T checkReferenceUse(JsonReader in) throws IOException {
    return null;
  }

  public ReferencesPolicy getPolicy() {
    return ReferencesPolicy.NONE;
  }

}
