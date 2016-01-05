package am.yagson.refs.impl;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import am.yagson.refs.ReferencesPolicy;
import am.yagson.refs.ReferencesReadContext;
import am.yagson.refs.ReferencesWriteContext;

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
    if (in.peek() == JsonToken.STRING) {
      throw new JsonSyntaxException("The reference cannot be read, as the current policy is ReferencesPolicy." +
              ReferencesPolicy.DISABLED + ": '" + in.nextString() + "'");
    }
    return null;
  }

  public ReferencesPolicy getPolicy() {
    return ReferencesPolicy.DISABLED;
  }

}
