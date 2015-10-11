package am.yagson;

public class ReferencesAllDuplicatesModeContextFactory implements ReferencesContextFactory {

  public ReferencesReadContext createReadContext() {
    return new ReferencesAllDuplicatesModeContext().new ReadContext();
  }

  public ReferencesWriteContext createWriteContext(Object root) {
    return new ReferencesAllDuplicatesModeContext().new WriteContext(root);
  }

}
