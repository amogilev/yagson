package am.yagson;

import com.google.gson.Gson;

public final class References {
  
  // forbidden
  private References() {}
  
  private static final ReferencesContextFactory defaultContextFactory = new ReferencesAllDuplicatesModeContextFactory();    
  
  public static String keyRef(int i) {
    return "" + i + "-key"; 
  }
  
  public static String valRef(int i) {
    return "" + i + "-val"; 
  }
  
  public static ReferencesReadContext createReadContext(Gson gson) {
    // TODO: select implementation based on the enabled features
    return defaultContextFactory.createReadContext();
  }
  
  public static ReferencesWriteContext createWriteContext(Gson gson, Object root) {
    return defaultContextFactory.createWriteContext(root);
  }
}
