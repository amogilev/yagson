package com.gilecode.yagson.refs.impl;

import com.gilecode.yagson.refs.ReferencesContextFactory;
import com.gilecode.yagson.refs.ReferencesReadContext;
import com.gilecode.yagson.refs.ReferencesWriteContext;

public class ReferencesNoneModeContextFactory implements ReferencesContextFactory {
  
  public ReferencesReadContext createReadContext() {
    return ReferencesNoneModeContext.instance;
  }

  public ReferencesWriteContext createWriteContext(Object root) {
    return ReferencesNoneModeContext.instance;
  }
}
