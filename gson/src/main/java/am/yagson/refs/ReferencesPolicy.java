package am.yagson.refs;

import am.yagson.refs.impl.ReferencesAllDuplicatesModeContextFactory;
import am.yagson.refs.impl.ReferencesCircularAndSiblingsContextFactory;
import am.yagson.refs.impl.ReferencesCircularOnlyModeContextFactory;
import am.yagson.refs.impl.ReferencesNoneModeContextFactory;

public enum ReferencesPolicy {

  /**
   * No references are allowed (same as in original Gson)
   */
  DISABLED(new ReferencesNoneModeContextFactory()),

  /**
   * Only the circular references are checked. This is the minimal mode required to avoid
   * {@link StackOverflowError}
   */
  CIRCULAR_ONLY(new ReferencesCircularOnlyModeContextFactory()),

  /**
   * Checks the circular references and duplicate fields in each object.
   * <p/>
   * This mode is default one, as it provides correct serialization of many standard collections (such as
   * Collections$SynchronizedSortedSet) while no so time-consuming as the full checks.
   */
  CIRCULAR_AND_SIBLINGS(new ReferencesCircularAndSiblingsContextFactory()),

  /**
   * 'Full' mode - all objects are checked for duplication.
   * <p/>
   * NOTE: This is the most CPU- and RAM- consuming mode
   */
  DUPLICATE_OBJECTS(new ReferencesAllDuplicatesModeContextFactory());

  private ReferencesContextFactory contextFactory;

  ReferencesPolicy(ReferencesContextFactory contextFactory) {
    this.contextFactory = contextFactory;
  }

  public ReferencesContextFactory getContextFactory() {
    return contextFactory;
  }

  public boolean isEnabled() { return this != DISABLED; }
}
