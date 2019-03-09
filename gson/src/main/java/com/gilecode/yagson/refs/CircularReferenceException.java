package com.gilecode.yagson.refs;

import com.google.gson.JsonIOException;

/**
 * An exception which is thrown when a circular reference is detected in a serialized value
 * and YaGson is in {@link ReferencesPolicy#DETECT_CIRCULAR_AND_THROW} mode.
 */
public class CircularReferenceException extends JsonIOException {

    public CircularReferenceException(String message) {
        super(message);
    }
}
