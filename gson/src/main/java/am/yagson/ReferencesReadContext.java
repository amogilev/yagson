package am.yagson;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

public interface ReferencesReadContext {
  
  /**
   * Registers an object corresponding to the path built by previous {@link #beforeObjectRead()} call.
   *  
   * @param value the object created by de-serializers, optionally without fields/contents yet
   * @param isSimple whether the object is known to not contain any circular dependencies, and so
   *    there will be no references to it
   */
  void registerObject(Object value, boolean isSimple);
  
  <T> T doRead(JsonReader reader, TypeAdapter<T> typeAdapter, String pathElement) throws IOException;
  
  <T> T checkReferenceUse(JsonReader in) throws IOException;
}
