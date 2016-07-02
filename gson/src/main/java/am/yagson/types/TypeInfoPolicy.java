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
    EMIT_TYPE_WRAPPERS;

    /**
     * Whether type info is emitted (no matter how exactly)
     */
    public boolean isEnabled() {
        return this != DISABLED;
    }

    public static TypeInfoPolicy defaultPolicy() {
        return EMIT_TYPE_WRAPPERS;
    }
}
