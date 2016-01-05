package am.yagson.refs.impl;

import am.yagson.refs.ReferencesContextFactory;
import am.yagson.refs.ReferencesReadContext;
import am.yagson.refs.ReferencesWriteContext;

public class ReferencesAllDuplicatesModeContextFactory implements ReferencesContextFactory {

  public ReferencesReadContext createReadContext() {
    return new ReferencesAllDuplicatesModeContext().new ReadContext();
  }

  public ReferencesWriteContext createWriteContext(Object root) {
    return new ReferencesAllDuplicatesModeContext().new WriteContext(root);
  }

}
