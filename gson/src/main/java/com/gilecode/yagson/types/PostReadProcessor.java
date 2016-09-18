package com.gilecode.yagson.types;

/**
 * A postprocessor which may be run after some specific class is deserialized
 */
public interface PostReadProcessor {

    void apply(Object instance);

    Iterable<String> getNamesOfProcessedClasses();
}
