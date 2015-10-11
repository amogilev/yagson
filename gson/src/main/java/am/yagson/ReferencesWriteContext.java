package am.yagson;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

public interface ReferencesWriteContext {
  
  public <T> JsonElement doToJsonTree(T value, TypeAdapter<T> valueTypeAdapter, String pathElement);
  
  public <T> void doWrite(T value, TypeAdapter<T> valueTypeAdapter, String pathElement, JsonWriter out) throws IOException;

}
