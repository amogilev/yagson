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

import java.lang.reflect.Field;

/**
 * An interface used to get an access to a {@link Field} stored in classes like
 * {@link com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.BoundField}
 *
 * @author Andrey Mogilev
 */
public interface HasField {

    /**
     * Returns the stored field.
     */
    Field getField();
}
