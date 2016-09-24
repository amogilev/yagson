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
package com.gilecode.yagson.refs.impl;

import com.gilecode.yagson.refs.ReferencePlaceholder;

import java.lang.reflect.Field;

/**
 * A special kind of the reference placeholders, used to return the reference to the
 * reflective field of the object being read, i.e. '@.field' references.
 */
public class FieldReferencePlaceholder<T> extends ReferencePlaceholder<T> {

    /**
     * The (serialization) field name, is known at creation time.
     */
    private final String referencedFieldName;

    /**
     * The field name, resolved to the actual field during the placeholder processing.
     */
    private Field referencedField;

    public FieldReferencePlaceholder(String referencedFieldName) {
        this.referencedFieldName = referencedFieldName;
    }

    public String getReferencedFieldName() {
        return referencedFieldName;
    }

    public Field getReferencedField() {
        return referencedField;
    }

    public void setReferencedField(Field referencedField) {
        this.referencedField = referencedField;
    }

    public boolean isResolved() {
        return referencedField != null;
    }

    @Override
    public String toString() {
        return "FieldReferencePlaceholder{" +
                "referencedFieldName='" + referencedFieldName + '\'' +
                "}";
    }
}
