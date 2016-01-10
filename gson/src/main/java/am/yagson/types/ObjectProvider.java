package am.yagson.types;

/**
 * Generic provider of an object, either existing or nwly constructed.
 *
 * @author Andrey Mogilev
 */
public interface ObjectProvider<T> {

    /**
     * Returns an object instance.
     */
    public T get();
}