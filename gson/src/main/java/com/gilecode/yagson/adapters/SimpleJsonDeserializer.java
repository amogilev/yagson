package com.gilecode.yagson.adapters;

import com.google.gson.JsonDeserializer;

public abstract class SimpleJsonDeserializer<T> implements JsonDeserializer<T> {

    @Override
    public boolean isSimple() {
        return true;
    }
}
