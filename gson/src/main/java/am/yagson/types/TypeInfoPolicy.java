package am.yagson.types;

public enum TypeInfoPolicy {

    /**
     * No type information emitted
     */
    DISABLED,

    /**
     * Emits required type info as type/val wrapper object, like <pre>{"@type":"org.my.MyType", "@val":orig_value}</pre>
     * instead of <pre>orig_value</pre>.
     * <p/>
     * No type info is emitted where the declared type equals to the runtime class.
     */
    EMIT_TYPE_WRAPPERS,

    /**
     * Emits required type info as "next value's type" for fields/values inside JSON objects or maps, and as type
     * wrappers for other places, like elements in arrays, keys in maps, or the root object.
     * <p/>
     * No type info is emitted where the declared type equals to the runtime class.
     */
    EMIT_WRAPPERS_OR_VTYPES;

    // TODO: how about root? rename to TypeInfoPolicyKind and add class TypeInfoPolicy?
    // or, it will be done through new toJson method with additional Type/Class deserializationType argument

    /**
     * Whether type info is emitted (no matter how exactly)
     */
    public boolean isEnabled() {
        return this != DISABLED;
    }

    public static TypeInfoPolicy defaultPolicy() {
        return EMIT_WRAPPERS_OR_VTYPES;
    }
}
