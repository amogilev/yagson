package am.yagson.strategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Excludes fields declared in one of the specified classes (not subclasses!).
 */
public class ExcludeByDeclaringClass implements ExclusionStrategy {

    private final Set<Class<?>> skipDeclaringClasses;

    public ExcludeByDeclaringClass(Class<?>...skipDeclaringClasses) {
        this.skipDeclaringClasses = new HashSet<Class<?>>(asList(skipDeclaringClasses));
    }

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return skipDeclaringClasses.contains(f.getDeclaringClass());
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
