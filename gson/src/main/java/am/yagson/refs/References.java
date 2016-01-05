package am.yagson.refs;

public final class References {
  
  // forbidden
  private References() {}
  
  private static final ReferencesPolicy defaultPolicy = ReferencesPolicy.CIRCULAR_ONLY;
  
  public static String keyRef(int i) {
    return "" + i + "-key"; 
  }
  
  public static String valRef(int i) {
    return "" + i + "-val"; 
  }
  
  public static ReferencesReadContext createReadContext(ReferencesPolicy policy) {
    if (policy == null) {
      policy = defaultPolicy;
    }
    
    return policy.getContextFactory().createReadContext();
  }
  
  public static ReferencesWriteContext createWriteContext(ReferencesPolicy policy, Object root) {
    if (policy == null) {
      policy = defaultPolicy;
    }
    
    return policy.getContextFactory().createWriteContext(root);
  }

  public static ReferencesPolicy defaultPolicy() {
    return defaultPolicy;
  }
}
