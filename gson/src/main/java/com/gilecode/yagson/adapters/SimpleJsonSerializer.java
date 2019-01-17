package com.gilecode.yagson.adapters;

import com.google.gson.JsonSerializer;

public abstract class SimpleJsonSerializer<T> implements JsonSerializer<T> {

    @Override
    public boolean isSimple() {
        return true;
    }
}
