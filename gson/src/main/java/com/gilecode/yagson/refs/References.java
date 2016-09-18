package com.gilecode.yagson.refs;

public final class References {

    /**
     * The prefix which starts each reference path.
     * <p/>
     * When a reference string with such prefix is used as a string value, the
     * object at the corresponding reference path shall be applied instead.
     */
    public static final String REF_ROOT = "@root";

    /**
     * The prefix which starts the field references.
     * <p/>
     * When a field reference is used as a key in JSON object for a Map or Collection,
     * it designates the name and field for a value in the instance object to be applied,
     * rather than a general element of that Map or Collection.
     */
    public static final String REF_FIELD_PREFIX = "@.";

    /**
     * The reference used for hashcode fields with value equals to the actual hashcode.
     */
    public static final String REF_HASH = "@hash";

    // forbidden
    private References() {
    }

    private static final ReferencesPolicy defaultPolicy = ReferencesPolicy.DUPLICATE_OBJECTS;

    public static String keyRef(int i) {
        return "" + i + "-key";
    }

    public static String valRef(int i) {
        return "" + i + "-val";
    }

    public static ReferencesReadContext createReadContext(ReferencesPolicy policy) {
        if (policy == null) {
            policy = defaultPolicy;
        }

        return policy.getContextFactory().createReadContext();
    }

    public static ReferencesWriteContext createWriteContext(ReferencesPolicy policy, Object root) {
        if (policy == null) {
            policy = defaultPolicy;
        }

        return policy.getContextFactory().createWriteContext(root);
    }

    public static ReferencesPolicy defaultPolicy() {
        return defaultPolicy;
    }
}
