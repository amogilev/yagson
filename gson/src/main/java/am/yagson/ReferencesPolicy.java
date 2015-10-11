package am.yagson;

import am.yagson.refs.impl.ReferencesAllDuplicatesModeContextFactory;
import am.yagson.refs.impl.ReferencesCircularOnlyModeContextFactory;
import am.yagson.refs.impl.ReferencesNoneModeContextFactory;

public enum ReferencesPolicy {
  
  NONE(new ReferencesNoneModeContextFactory()),
  
  DUPLICATE_OBJECTS(new ReferencesAllDuplicatesModeContextFactory()),
  
  CIRCULAR_ONLY(new ReferencesCircularOnlyModeContextFactory());
  
  private ReferencesContextFactory contextFactory;

  private ReferencesPolicy(ReferencesContextFactory contextFactory) {
    this.contextFactory = contextFactory;
  }

  public ReferencesContextFactory getContextFactory() {
    return contextFactory;
  }
}
