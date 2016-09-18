package com.gilecode.yagson.types;

import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;

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

    public static <E> ObjectProvider<E> defaultOf(E instance, ConstructorConstructor cc) {
        TypeToken<E> typeToken = (TypeToken<E>) TypeToken.get(instance.getClass());
        return new ConstructingObjectProvider<E>(cc.get(typeToken));
    }
}
