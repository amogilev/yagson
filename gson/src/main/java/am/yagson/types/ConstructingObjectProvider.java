package am.yagson.types;

import com.google.gson.internal.ObjectConstructor;

/**
 * Object provider which wraps {@link ObjectConstructor} used to
 * create an object instance.
 */
public class ConstructingObjectProvider<T> implements ObjectProvider<T> {

    private final ObjectConstructor<T> constructor;

    public ConstructingObjectProvider(ObjectConstructor<T> constructor) {
        this.constructor = constructor;
    }

    /**
     * Returns an object instance.
     */
    public T get() {
        return constructor.construct();
    }
}
