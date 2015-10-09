package am.yagson;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

/**
 * Used to find circular dependencies and duplciate references during the 
 * serialization.
 * 
 * @author Andrey Mogilev
 */
public class ReferencesContext {
  
  IdentityHashMap<Object, String> references = new IdentityHashMap<Object, String>();
  Deque<String> currentPathElements = new ArrayDeque<String>();
  Deque<Object> currentObjects = new ArrayDeque<Object>();
  
  public ReferencesContext(Object root) {
    startObject(root, "@root");
  }

  public void reinit() {
    references.clear();
    currentPathElements.clear();
    currentObjects.clear();
  }

  /**
   * Returns non-null reference path if the object was already visited
   * during serialization in the current context, or saves the object as
   * visited and returns null. 
   *  
   * @param value the object to be serialized next
   * @param pathElement the element corresponding to teh object in the references path
   * 
   * @return null if object needs to be serialized in a regular way, or a reference path 
   *    if it is already visited and so the returned reference shall be used instead  
   */
  public String startObject(Object value, String pathElement) {
    String ref = references.get(value);
    if (ref != null) {
      return ref;
    }
    
    currentObjects.addLast(value);
    currentPathElements.addLast(pathElement);
    references.put(value, getCurrentPath());
    
    return null;
  }

  /**
   * Must be invoked in pair to {@link #startObject()} which returned null, after
   * serialization of the object is completed.
   *    
   * @param value the object which serialization has been completed
   */
  public void endObject(Object value) {
    Object last = currentObjects.pollLast();
    if (last != value) {
      throw new IllegalStateException("Out-of-order endObject()");
    }
    currentPathElements.removeLast();
  }
  
  public <T> JsonElement doToJsonTree(T value, TypeAdapter<T> valueTypeAdapter, String pathElement) {
    String ref = startObject(value, pathElement);
    if (ref != null) {
      return makeReferenceElement(ref);
    } else {
      JsonElement el = valueTypeAdapter.toJsonTree(value, this);
      endObject(value);
      return el;
    }
  }
  
  public <T> void doWrite(T value, TypeAdapter<T> valueTypeAdapter, String pathElement, JsonWriter out) throws IOException {
    String ref = startObject(value, pathElement);
    if (ref != null) {
      out.value(ref);
    } else {
      valueTypeAdapter.write(out, value, this);
      endObject(value);
    }
  }
  
  private JsonElement makeReferenceElement(String ref) {
    return new JsonPrimitive(ref);
  }

  private String getCurrentPath() {
    // TODO: optimize - cache last, maybe keep paths in stack etc.
    StringBuilder sb = new StringBuilder();
    for (String el : currentPathElements) {
      sb.append(el).append('.');
    }
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }


}
