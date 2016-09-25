/*
 * Copyright (C) 2016 Andrey Mogilev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gilecode.yagson.types;

import com.google.gson.TypeAdapter;
import java.lang.reflect.Field;

/**
 * Information about a reflective field, which includes the field, its default value,
 * and the type adapter.
 *
 * @author Andrey Mogilev
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
