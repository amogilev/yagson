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

/**
 * Object provider which returns existing instance of the object.
 */
public class ExistingObjectProvider<T> implements ObjectProvider<T> {

    private final T instance;

    public ExistingObjectProvider(T instance) {
        this.instance = instance;
    }

    /**
     * Returns an object instance.
     */
    public T get() {
        return instance;
    }

    public static <E> ObjectProvider<E> of(E instance) {
        return new ExistingObjectProvider<E>(instance);
    }
}
