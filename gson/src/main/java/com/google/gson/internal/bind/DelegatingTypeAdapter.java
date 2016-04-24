package com.google.gson.internal.bind;

import com.google.gson.TypeAdapter;

/**
 * Abstract for type adapter wrappers with known delegates.
 * Once initialized, returns the same delegate on each call to {@link #getDelegate()} }
 */
public abstract class DelegatingTypeAdapter<T> extends TypeAdapter<T> {

    protected TypeAdapter<T> delegate;

    public TypeAdapter<T> getDelegate() {
        return delegate;
    }
}
