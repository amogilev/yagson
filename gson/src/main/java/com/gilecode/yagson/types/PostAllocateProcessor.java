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
 * A postprocessor which may be run after some specific class is constructed by the unsafe allocator.
 * <p/>
 * In particular, is used to init 'key' type in the {@link java.util.EnumMap} instances.
 *
 * @author Andrey Mogilev
 */
public interface PostAllocateProcessor {

    /**
     * Process the newly created object instance.
     */
    void apply(Object instance);
}
