package com.gilecode.yagson.types;

/**
 * A postprocessor which may be run after some specific class is constructed by the unsafe allocator.
 */
public interface PostAllocateProcessor {

    void apply(Object instance);
}
