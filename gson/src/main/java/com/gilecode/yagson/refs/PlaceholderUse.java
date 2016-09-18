package com.gilecode.yagson.refs;

import java.io.IOException;

/**
 * Specifies the use site of the reference placeholder, such as an object field, an array's element, or map's key
 * or value. When the actual object is instantiated, it is applied to all registered use sites.
 */
public interface PlaceholderUse<T> {

    /**
     * Sets the actual object to this use site, e.g. assigns it to a field, to an array's element etc.
     *
     * @param actualObject the actual object crated as the replacement of a placeholder
     */
    void applyActualObject(T actualObject) throws IOException;
}
