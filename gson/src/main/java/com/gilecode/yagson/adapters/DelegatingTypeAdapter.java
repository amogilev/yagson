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
package com.gilecode.yagson.adapters;

import com.google.gson.TypeAdapter;

/**
 * An abstract for type adapter wrappers with known delegates.
 * Once initialized, returns the same delegate on each call to {@link #getDelegate()} }
 *
 * @author Andrey Mogilev
 */
public abstract class DelegatingTypeAdapter<T> extends TypeAdapter<T> {

    @Override
    public boolean isSimple() {
        return delegate.isSimple();
    }

    protected TypeAdapter<T> delegate;

    public TypeAdapter<T> getDelegate() {
        return delegate;
    }
}
