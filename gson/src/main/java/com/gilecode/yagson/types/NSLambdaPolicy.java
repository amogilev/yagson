/*
 * Copyright (C) 2017 Andrey Mogilev
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
 * Enumerates supported policies for handling non-serializable lambdas.
 * <p/>
 * The current default option is to skip them (map to nulls).
 *
 * @author Andrey Mogilev
 */
public enum NSLambdaPolicy {

    /**
     * Skip non-serializable lambdas, i.e. serialize to nulls (which are usually skipped).
     */
    TO_NULL,

    /**
     * Throw a {@link NonSerializableLambdaException} if non-serializable lambda is found.
     */
    ERROR;

    // TODO maybe add "serialize to @nsLambda" mode
}
