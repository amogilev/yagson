package am.yagson.refs.impl;

import am.yagson.ReferencesContextFactory;
import am.yagson.ReferencesReadContext;
import am.yagson.ReferencesWriteContext;

public class ReferencesNoneModeContextFactory implements ReferencesContextFactory {
  
  public ReferencesReadContext createReadContext() {
    return ReferencesNoneModeContext.instance;
  }

  public ReferencesWriteContext createWriteContext(Object root) {
    return ReferencesNoneModeContext.instance;
  }
}
