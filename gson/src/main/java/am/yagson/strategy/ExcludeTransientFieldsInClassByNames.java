package am.yagson.strategy;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Excludes transient fields in the specified class or its subclasses by names.
 */
public class ExcludeTransientFieldsInClassByNames implements TransientFieldExclusionStrategy {

    private final Class<?> declaringSuperClass;
    private final Set<String> skipFieldNames;

    public ExcludeTransientFieldsInClassByNames(Class<?> declaringSuperClass, String...fieldNames) {
        this.declaringSuperClass = declaringSuperClass;
        this.skipFieldNames = new HashSet<String>(asList(fieldNames));
    }

    @Override
    public boolean shouldSkipField(Field f) {
        return declaringSuperClass.isAssignableFrom(f.getDeclaringClass()) && skipFieldNames.contains(f.getName());
    }
}
