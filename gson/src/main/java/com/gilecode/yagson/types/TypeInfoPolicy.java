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
