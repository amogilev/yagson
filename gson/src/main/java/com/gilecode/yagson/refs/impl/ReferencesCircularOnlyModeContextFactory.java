package com.gilecode.yagson.refs.impl;

import com.gilecode.yagson.refs.ReferencesContextFactory;
import com.gilecode.yagson.refs.ReferencesReadContext;
import com.gilecode.yagson.refs.ReferencesWriteContext;

public class ReferencesCircularOnlyModeContextFactory implements ReferencesContextFactory {

  public ReferencesReadContext createReadContext() {
    return new ReferencesCircularOnlyModeContext().new RefsReadContext();
  }

  public ReferencesWriteContext createWriteContext(Object root) {
    return new ReferencesCircularOnlyModeContext().new RefsWriteContext(root);
  }

}
