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
package com.gilecode.yagson.refs;

/**
 * An abstract factory which creates {@link ReferencesReadContext} and {@link ReferencesWriteContext}.
 *
 * @author Andrey Mogilev
 */
public interface ReferencesContextFactory {

    /**
     * Creates and returns a read context.
     */
    ReferencesReadContext createReadContext();

    /**
     * Creates and returns a write context with a specified root object.
     */
    ReferencesWriteContext createWriteContext(Object root);
}
