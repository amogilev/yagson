package com.gilecode.yagson.types;

import java.lang.reflect.Type;

/**
 * The predicate (more formally, bi-predicate) which decides whether the type info shall
 * be emitted for the given combination of the actual class and the formal (de-)serialization
 * type.
 * <p/>
 * There may be different implementations of this predicate depending on the circumstances. For example,
 * the rules for the map's keys differ from the general rules.
 */
public interface EmitTypeInfoPredicate {

    /**
     * Returns whether the type info shall be emitted for the given combination of the actual class and the
     * formal (de-)serialization type.
     *
     * @param actualClass the actual class of the object being serialized
     * @param formalType the corresponding (de-)serialization type
     *
     * @return whether to emit type info
     */
    boolean apply(Class<?> actualClass, Type formalType);
}
