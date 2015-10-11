package am.yagson.refs.impl;

import am.yagson.ReferencesContextFactory;
import am.yagson.ReferencesReadContext;
import am.yagson.ReferencesWriteContext;

public class ReferencesCircularOnlyModeContextFactory implements ReferencesContextFactory {

  public ReferencesReadContext createReadContext() {
    return new ReferencesCircularOnlyModeContext().new ReadContext();
  }

  public ReferencesWriteContext createWriteContext(Object root) {
    return new ReferencesCircularOnlyModeContext().new WriteContext(root);
  }

}
