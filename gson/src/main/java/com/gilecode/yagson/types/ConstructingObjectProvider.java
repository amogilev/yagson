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

import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;

/**
 * Object provider which wraps {@link ObjectConstructor} used to
 * create an object instance.
 */
public class ConstructingObjectProvider<T> implements ObjectProvider<T> {

    private final ObjectConstructor<T> constructor;

    public ConstructingObjectProvider(ObjectConstructor<T> constructor) {
        this.constructor = constructor;
    }

    /**
     * Returns an object instance.
     */
    public T get() {
        return constructor.construct();
    }

    public static <E> ObjectProvider<E> defaultOf(E instance, ConstructorConstructor cc) {
        TypeToken<E> typeToken = (TypeToken<E>) TypeToken.get(instance.getClass());
        return new ConstructingObjectProvider<E>(cc.get(typeToken));
    }
}
