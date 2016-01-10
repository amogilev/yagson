package am.yagson.types;

/**
 * Object provider which returns existing instance of the object.
 */
public class ExistingObjectProvider<T> implements ObjectProvider<T> {

    private final T instance;

    public ExistingObjectProvider(T instance) {
        this.instance = instance;
    }

    /**
     * Returns an object instance.
     */
    public T get() {
        return instance;
    }
}
