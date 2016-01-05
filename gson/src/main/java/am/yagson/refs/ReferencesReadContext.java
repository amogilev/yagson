package am.yagson.refs;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;
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

  /**
   * If the next token is a string, that string is considered as a reference, and the
   * object corresponding to that reference is returned.
   *
   * @throws JsonSyntaxException if no referenced object is known for the read reference
   * @throws IOException if read failed
     */
  <T> T checkReferenceUse(JsonReader in) throws IOException;
  
  ReferencesPolicy getPolicy();
}
