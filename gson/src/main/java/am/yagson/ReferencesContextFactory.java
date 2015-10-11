package am.yagson;

public interface ReferencesContextFactory {
  
  ReferencesReadContext createReadContext();
  
  ReferencesWriteContext createWriteContext(Object root);
}
