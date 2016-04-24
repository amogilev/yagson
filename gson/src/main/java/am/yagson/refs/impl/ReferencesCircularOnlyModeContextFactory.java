package am.yagson.refs.impl;

import am.yagson.refs.ReferencesContextFactory;
import am.yagson.refs.ReferencesReadContext;
import am.yagson.refs.ReferencesWriteContext;

public class ReferencesCircularOnlyModeContextFactory implements ReferencesContextFactory {

  public ReferencesReadContext createReadContext() {
    return new ReferencesCircularOnlyModeContext().new RefsReadContext();
  }

  public ReferencesWriteContext createWriteContext(Object root) {
    return new ReferencesCircularOnlyModeContext().new RefsWriteContext(root);
  }

}
