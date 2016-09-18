package com.gilecode.yagson.refs;

public interface ReferencesContextFactory {
  
  ReferencesReadContext createReadContext();
  
  ReferencesWriteContext createWriteContext(Object root);
}
