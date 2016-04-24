package am.yagson.refs.impl;

import am.yagson.refs.ReferencesPolicy;
import com.google.gson.JsonSyntaxException;

/**
 * Used to find circular dependencies and duplicate references during the 
 * serialization.
 * 
 * @author Andrey Mogilev
 */
public class ReferencesCircularOnlyModeContext extends ReferencesAllDuplicatesModeContext {
  
  static ReferencesPolicy policy = ReferencesPolicy.CIRCULAR_ONLY;
  
  class RefsWriteContext extends ReferencesAllDuplicatesModeContext.RefsWriteContext {
    public RefsWriteContext(Object root) {
      super(root);
    }

    @Override
    public ReferencesPolicy getPolicy() {
      return policy;
    }

    @Override
    protected void endObject(Object value) {
      super.endObject(value);
      if (value != null) {
        references.remove(value);        
      }
    }
  }
  
  class RefsReadContext extends ReferencesAllDuplicatesModeContext.RefsReadContext {

    @Override
    public ReferencesPolicy getPolicy() {
      return policy;
    }

    @Override
    protected void afterObjectRead() {
      objectsByReference.remove(getCurrentReference());
      super.afterObjectRead();
    }

    @Override
    protected Object getObjectByReference(String reference) throws JsonSyntaxException {
      Object value = objectsByReference.get(reference);
      if (value == null) {
        if (!getCurrentReference().contains(reference)) {
          throw new JsonSyntaxException("The reference cannot be read, as the current ReferencesPolicy " +
                  "allows only circular references: '" + reference + "'");
        }
        throw new JsonSyntaxException("Missing reference '" + reference + "'");
      }
      return value;
    }
  }
}
