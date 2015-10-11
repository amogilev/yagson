package am.yagson.refs.impl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import am.yagson.ReferencesPolicy;
import am.yagson.ReferencesReadContext;
import am.yagson.ReferencesWriteContext;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Used to find circular dependencies and duplicate references during the 
 * serialization.
 * 
 * @author Andrey Mogilev
 */
public class ReferencesAllDuplicatesModeContext {
  
  static ReferencesPolicy policy = ReferencesPolicy.DUPLICATE_OBJECTS;
  
  protected Deque<String> currentPathElements = new ArrayDeque<String>();
  
  protected String getCurrentReference() {
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
  
  
  class WriteContext implements ReferencesWriteContext {
    protected IdentityHashMap<Object, String> references = new IdentityHashMap<Object, String>();
    protected Deque<Object> currentObjects = new ArrayDeque<Object>(); // used only for self-checks
    
    public WriteContext(Object root) {
      startObject(root, "@root");
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
    protected String startObject(Object value, String pathElement) {
      if (value != null) {
        String ref = references.get(value);
        if (ref != null) {
          return ref;
        }
        
        currentObjects.addLast(value);
        currentPathElements.addLast(pathElement);
        references.put(value, getCurrentReference());
      }
      
      return null;
    }
    
    /**
     * Must be invoked in pair to {@link #startObject()} which returned null, after
     * serialization of the object is completed.
     *    
     * @param value the object which serialization has been completed
     */
    protected void endObject(Object value) {
      if (value != null) {
        Object last = currentObjects.pollLast();
        if (last != value) {
          throw new IllegalStateException("Out-of-order endObject()");
        }
        currentPathElements.removeLast();
      }
    }
    
    public <T> JsonElement doToJsonTree(T value, TypeAdapter<T> valueTypeAdapter, String pathElement) {
      if (valueTypeAdapter.hasSimpleJsonFor(value)) {
        return valueTypeAdapter.toJsonTree(value, this);
      }
      
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
      if (valueTypeAdapter.hasSimpleJsonFor(value)) {
        valueTypeAdapter.write(out, value, this);
        return;
      }
      
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

    public ReferencesPolicy getPolicy() {
      return policy;
    }
  }
  
  
  class ReadContext implements ReferencesReadContext {
    
    protected boolean awaitsObjectRead = false;
    protected Map<String, Object> objectsByReference = new HashMap<String, Object>();  
        
    public ReadContext() {
      currentPathElements.add("@root");
      awaitsObjectRead = true;
    }
    
    protected void beforeObjectRead(String pathElement) {
      if (awaitsObjectRead) {
        throw new IllegalStateException("Awaits object, but get another path element: " + getCurrentReference());
      }
      currentPathElements.addLast(pathElement);
      awaitsObjectRead = true;
    }

    /**
     * Registers an object corresponding to the path built by previous {@link #beforeObjectRead()} call.
     *  
     * @param value the object created by de-serializers, optionally without fields/contents yet
     * @param isSimple whether the object is known to not contain any circular dependencies, and so
     *    there will be no references to it
     */
    public void registerObject(Object value, boolean isSimple) {
      if (!awaitsObjectRead) {
        throw new IllegalStateException("registerObject() without corresponding beforeObjectRead(): " + getCurrentReference());
      }
      awaitsObjectRead = false;
      if (!isSimple && value != null) {
        objectsByReference.put(getCurrentReference(), value);
      }
    }
    
    protected Object registerReferenceUse(String reference) {
      Object value = getObjectByReference(reference);
      // the object may now be reference both with the used and the current reference 
      registerObject(value, false);
      return value;
    }

    protected void afterObjectRead() {
      if (awaitsObjectRead) {
        throw new IllegalStateException("afterObjectRead() without corresponding registerObject(): " + getCurrentReference());
      }
      currentPathElements.removeLast();
    }
    
    
    protected Object getObjectByReference(String reference) throws JsonSyntaxException {
      Object value = objectsByReference.get(reference);
      if (value == null) {
        throw new JsonSyntaxException("Missing reference '" + reference + "'");
      }
      return value;
    }

    public <T> T doRead(JsonReader reader, TypeAdapter<T> typeAdapter, String pathElement) throws IOException {
      beforeObjectRead(pathElement);
      T fieldValue = typeAdapter.read(reader, this);
      if (fieldValue == null) {
        // registerObject is skipped for nulls in most cases, so clear 'awaits' flag
        awaitsObjectRead = false;
      }
      afterObjectRead();
      return fieldValue;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T checkReferenceUse(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.STRING) {
        String reference = in.nextString();
        try {
          return (T) registerReferenceUse(reference);
        } catch (ClassCastException e) {
          throw new JsonSyntaxException("Incompatible reference type used: " + reference, e);
        }
      }
      
      return null;
    }
    
    public ReferencesPolicy getPolicy() {
      return policy;
    }
  }
}
