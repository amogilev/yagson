package com.gilecode.yagson.types;

import com.google.gson.TypeAdapter;
import java.lang.reflect.Field;

/**
 * Information about a reflective field, which includes the field, its default value,
 * and the type adapter.
 */
public class FieldInfo {
    private final Field field;
    private final Object defaultValue;
    private final TypeAdapter<Object> fieldAdapter;

    public FieldInfo(Field field, Object defaultValue, TypeAdapter<Object> fieldAdapter) {
        this.field = field;
        this.defaultValue = defaultValue;
        this.fieldAdapter = fieldAdapter;
    }

    public Field getField() {
        return field;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public TypeAdapter<Object> getFieldAdapter() {
        return fieldAdapter;
    }
}
