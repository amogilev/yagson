package am.yagson.refs.impl;

import am.yagson.refs.ReferencePlaceholder;

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
