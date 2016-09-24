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

import java.lang.reflect.Type;

/**
 * The predicate (more formally, bi-predicate) which decides whether the type info shall
 * be emitted for the given combination of the actual class and the formal (de-)serialization
 * type.
 * <p/>
 * There may be different implementations of this predicate depending on the circumstances. For example,
 * the rules for the map's keys differ from the general rules.
 */
public interface EmitTypeInfoPredicate {

    /**
     * Returns whether the type info shall be emitted for the given combination of the actual class and the
     * formal (de-)serialization type.
     *
     * @param actualClass the actual class of the object being serialized
     * @param formalType the corresponding (de-)serialization type
     *
     * @return whether to emit type info
     */
    boolean apply(Class<?> actualClass, Type formalType);
}
