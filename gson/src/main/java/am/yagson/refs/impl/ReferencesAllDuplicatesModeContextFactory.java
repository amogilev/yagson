package am.yagson.refs.impl;

import am.yagson.ReferencesContextFactory;
import am.yagson.ReferencesReadContext;
import am.yagson.ReferencesWriteContext;

public class ReferencesAllDuplicatesModeContextFactory implements ReferencesContextFactory {

  public ReferencesReadContext createReadContext() {
    return new ReferencesAllDuplicatesModeContext().new ReadContext();
  }

  public ReferencesWriteContext createWriteContext(Object root) {
    return new ReferencesAllDuplicatesModeContext().new WriteContext(root);
  }

}
