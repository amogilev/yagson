package am.yagson.refs;

import java.lang.reflect.Field;

/**
 * The placeholder for an object's hash code value.
 */
public class HashReferencePlaceholder extends ReferencePlaceholder<Integer> {

    private final Field field;

    public HashReferencePlaceholder(Field field) {
        super(Integer.class);
        this.field = field;
    }
}
