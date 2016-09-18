package com.gilecode.yagson.refs.impl;

import com.gilecode.yagson.refs.ReferencesContextFactory;
import com.gilecode.yagson.refs.ReferencesReadContext;
import com.gilecode.yagson.refs.ReferencesWriteContext;

public class ReferencesAllDuplicatesModeContextFactory implements ReferencesContextFactory {

  public ReferencesReadContext createReadContext() {
    return new ReferencesAllDuplicatesModeContext().new RefsReadContext();
  }

  public ReferencesWriteContext createWriteContext(Object root) {
    return new ReferencesAllDuplicatesModeContext().new RefsWriteContext(root);
  }

}
