package am.yagson.refs.impl;

import am.yagson.ReferencesPolicy;

/**
 * Used to find circular dependencies and duplicate references during the 
 * serialization.
 * 
 * @author Andrey Mogilev
 */
public class ReferencesCircularOnlyModeContext extends ReferencesAllDuplicatesModeContext {
  
  static ReferencesPolicy policy = ReferencesPolicy.CIRCULAR_ONLY;
  
  class WriteContext extends ReferencesAllDuplicatesModeContext.WriteContext {
    public WriteContext(Object root) {
      super(root);
    }

    @Override
    public ReferencesPolicy getPolicy() {
      return policy;
    }

    @Override
    protected void endObject(Object value) {
      if (value != null) {
        references.remove(value);        
      }
    }
  }
  
  class ReadContext extends ReferencesAllDuplicatesModeContext.ReadContext {

    @Override
    public ReferencesPolicy getPolicy() {
      return policy;
    }

    @Override
    protected void afterObjectRead() {
      objectsByReference.remove(getCurrentReference());
      super.afterObjectRead();
    }
  }
}
