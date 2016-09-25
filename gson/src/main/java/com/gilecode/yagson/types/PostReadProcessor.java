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
 * A postprocessor which may be run after a class is deserialized, to apply
 * actions specific for this class.
 *
 * @author Andrey Mogilev
 */
public interface PostReadProcessor {

    /**
     * Apply actions for the de-serialized object
     *
     * @param instance a de-serialized object to process
     */
    void apply(Object instance);

    /**
     * Returns the names of the classes to be processed by this processor.
     */
    Iterable<String> getNamesOfProcessedClasses();
}
