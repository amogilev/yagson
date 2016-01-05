package am.yagson.refs.impl;

import am.yagson.refs.ReferencesContextFactory;
import am.yagson.refs.ReferencesReadContext;
import am.yagson.refs.ReferencesWriteContext;

public class ReferencesNoneModeContextFactory implements ReferencesContextFactory {
  
  public ReferencesReadContext createReadContext() {
    return ReferencesNoneModeContext.instance;
  }

  public ReferencesWriteContext createWriteContext(Object root) {
    return ReferencesNoneModeContext.instance;
  }
}
