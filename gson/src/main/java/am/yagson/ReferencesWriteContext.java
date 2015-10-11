package am.yagson;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

public interface ReferencesWriteContext {
  
  <T> JsonElement doToJsonTree(T value, TypeAdapter<T> valueTypeAdapter, String pathElement);
  
  <T> void doWrite(T value, TypeAdapter<T> valueTypeAdapter, String pathElement, JsonWriter out) throws IOException;
  
  ReferencesPolicy getPolicy();

}
